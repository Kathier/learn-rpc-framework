package learnfast.pankai.transport.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import learnfast.pankai.constants.RpcConstants;
import learnfast.pankai.dto.RpcMessage;
import learnfast.pankai.enumration.SerializationTypeEnum;
import learnfast.pankai.extension.ExtensionLoader;
import learnfast.pankai.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author WangTao
 * @createTime on 2020/10/2
 *
 * 自定义协议解码器
 *
 *  * <pre>
 *  * 0     1     2     3     4        5     6     7     8     9          10       11     12    13    14   15
 *  * +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+-----------+-----+-----+-----+
 *  * |   magic   code        |version | Full length         | messageType| codec| RequestId                   |
 *  * +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *  * |                                                                                                       |
 *  * |                                         body                                                          |
 *  * |                                                                                                       |
 *  * |                                        ... ...                                                        |
 *  * +-------------------------------------------------------------------------------------------------------+
 *
 *  自定义编码器
 *  4B  magic   code 魔法数  1B version 版本  4B full length  消息长度  1B messageType 消息类型
 *   1B codec 序列化   4B  requestId 请求的Id
 *   body object类型数据
 *
 */

@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);


    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out)
            throws Exception {
        try {
            int fullLength = RpcConstants.HEAD_LENGTH;
            byte messageType = rpcMessage.getMessageType();
            //写入magic数字
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            // 留出位置写入数据包的长度
            out.writerIndex(out.writerIndex() + 4);
            //设置消息类型
            out.writeByte(rpcMessage.getMessageType());
            //设置序列化类型
            out.writeByte(rpcMessage.getCodec());
            //设置requestId
            out.writeInt(ATOMIC_INTEGER.getAndDecrement());
            byte[] bodyBytes = null;

            //不是心跳
            // if messageType is not heartbeat message,fullLength = head length + body length
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                //对象序列化
                //获取序列化类型
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                //通过SPI机制选择序列化接口的具体实现
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                //序列化
                bodyBytes = serializer.serialize(rpcMessage.getData());
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }

            int writeIndex = out.writerIndex();
            //把写索引置为本应是消息长度的位置即在版本号之后
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            //写入长度
            out.writeInt(fullLength);
            //重置
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }


    }



}


