package learnfast.pankai.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import learnfast.pankai.serialize.Serializer;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by PanKai on 2021/2/20 11:00
 *
 * @Description
 **/
@AllArgsConstructor
public class NettyKryoDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(NettyKryoDecoder.class);
    private Serializer serializer;
    private  Class<?> genericClass;
    //netty传输的消息长度就是对象序列化后字节数组长度，存储在byteBuf头部
    private  static final int BODY_LENGTH=4;
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //byteBuf中写入的消息长度所占的字节数已经是4，所以可读的长度要大于4
        if(byteBuf.readableBytes()>BODY_LENGTH){
            //标记当前readIndex的位置，以便后续重置时使用
            byteBuf.markReaderIndex();
            int  dataLength=byteBuf.readInt();
            if(dataLength<0){
                return ;
            }
            //如果可读字节小于消息长度，说明消息不完整，重置readIndex
            if(byteBuf.readableBytes()<dataLength){
                byteBuf.resetReaderIndex();
                return;
            }
            byte [] body=new byte[dataLength];
            byteBuf.readBytes(body);
            Object o=serializer.deserialize(body,genericClass);
            list.add(o);
        }
    }
}
