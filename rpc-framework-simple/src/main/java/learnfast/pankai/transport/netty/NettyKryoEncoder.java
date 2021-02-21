package learnfast.pankai.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.MessageToByteEncoder;
import learnfast.pankai.serialize.KryoSerializer;
import learnfast.pankai.serialize.Serializer;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Created by PanKai on 2021/2/20 11:04
 *
 * @Description
 **/
@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder<Object> {
    private Serializer serializer;
    private  Class<?> genericClass;

    /**
     * 将对象转换为字节码写入到byteBuf中
     * @param channelHandlerContext
     * @param o
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if(genericClass.isInstance(o)){
            //将对象转换为字节数组
            byte [] body=serializer.serialize(o);
            //读取消息长度
            int length=body.length;
            //写入消息数组长度，writeIndex+4
            byteBuf.writeInt(length);
            //将字节数组写入到byteBuf
            byteBuf.writeBytes(body);
        }
    }
}
