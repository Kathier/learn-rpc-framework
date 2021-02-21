package learnfast.pankai.transport.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import learnfast.pankai.serialize.Serializer;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by PanKai on 2021/2/20 11:00
 * 自定义解码器，负责处理入站消息，将消息格式转换为我们需要的业务对象
 * @Description
 **/
@AllArgsConstructor
public class NettyKryoDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(NettyKryoDecoder.class);
    private Serializer serializer;
    private  Class<?> genericClass;
    //netty传输的消息长度就是对象序列化后字节数组长度，存储在byteBuf头部
    private  static final int BODY_LENGTH=4;

    /**
     *
     * @param channelHandlerContext 解码器关联的对象
     * @param byteBuf  入站数据
     * @param list  解码之后的数据对象需要添加到list中
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //byteBuf中写入的消息长度所占的字节数已经是4，所以可读的长度要大于4
        if(byteBuf.readableBytes()>BODY_LENGTH){
            //标记当前readIndex的位置，以便后续重置时使用
            byteBuf.markReaderIndex();
            int  dataLength=byteBuf.readInt();
            if(dataLength<0){
                logger.error("data length or byteBuf readableBytes is not valid");
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
            logger.info("successful decode ByteBuf to Object");
        }
    }
}
