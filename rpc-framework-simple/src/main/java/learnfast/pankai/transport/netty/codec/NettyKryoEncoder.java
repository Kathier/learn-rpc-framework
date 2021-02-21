package learnfast.pankai.transport.netty.codec;

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
 * 自定义编码器，负责处理“出站”消息，将消息格式转换为字节数组，写入到字节数据容器byteBuf对象中
 * 网络传输需要通过字节流来实现，byteBuf可以看作netty提供的字节数据容器
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
