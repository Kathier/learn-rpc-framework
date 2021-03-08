package learnfast.pankai.transport.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import learnfast.pankai.constants.RpcConstants;
import learnfast.pankai.dto.RpcMessage;
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.enumration.SerializationTypeEnum;
import learnfast.pankai.extension.ExtensionLoader;
import learnfast.pankai.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * Created by PanKai on 2021/3/8 17:02
 * 继承自定义长度解码器解决TCP粘包粘包问题
 * 1. LengthFieldBasedFrameDecoder本质上是ChannelHandler，一个处理入站事件的ChannelHandler
 * 2. LengthFieldBasedFrameDecoder需要加入ChannelPipeline中，且位于链的头部
 **/
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    public RpcMessageDecoder() {
        // default is 8M
        this(RpcConstants.MAX_FRAME_LENGTH);
    }

    public RpcMessageDecoder(int maxFrameLength) {
    /**
     *  发送的数据帧最大长度
        int maxFrameLength,
        定义长度域位于发送的字节数组中的下标。
        int lengthFieldOffset,  magic code is 4B, and version is 1B, and then FullLength. so value is 5
        用于描述定义的长度域的长度
        int lengthFieldLength,  FullLength is int(4B). so values is 4
        满足公式: 发送数据包长度 = 长度域的值 + lengthFieldOffset + lengthFieldLength + lengthAdjustment
        int lengthAdjustment,   FullLength include all data and read 9 bytes before, so the left length is (FullLength-9). so values is -9
        接收到的发送数据包，去除前initialBytesToStrip位
        int initialBytesToStrip we will check magic code and version self, so do not strip any bytes. so values is 0
        */
        super(maxFrameLength, 5, 4, -9, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }

        }
        return decoded;
    }



    private Object decodeFrame(ByteBuf in)
            throws Exception {
        //读取前4个magic比对一下
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
        int fullLength = in.readInt();
        //消息类型
        byte messageType = in.readByte();
        //读取序列化类型
        byte codecType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setMessageType(messageType);
        rpcMessage.setRequestId(requestId);
        rpcMessage.setCodec(codecType);

        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
        } else if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
        } else {
            //真正的消息体长度
            int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
            if (bodyLength > 0) {
                byte[] bs = new byte[bodyLength];
                in.readBytes(bs);
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                //请求数据
                if (messageType == RpcConstants.REQUEST_TYPE) {
                    RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
                    rpcMessage.setData(tmpValue);
                } else {
                    //响应数据
                    RpcResponse tmpValue = serializer.deserialize(bs, RpcResponse.class);
                    rpcMessage.setData(tmpValue);
                }
            }
        }
        return rpcMessage;

    }

}

