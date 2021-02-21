package learnfast.pankai.transport.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by PanKai on 2021/2/20 11:46
 *
 * @Description
 **/
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        RpcResponse rpcResponse = (RpcResponse) msg;
        logger.info(String.format("client recieve msg : %s",msg));
        //声明一个AttributeKey对象
        AttributeKey<RpcResponse> key=AttributeKey.valueOf("RpcResponse");
        //将服务端的返回结果保存到AttributeMap上，AttributeMap可以看作是channel的共享数据源
        //key是AttributeKey,value是Attribute
        channelHandlerContext.channel().attr(key).set(rpcResponse);
        channelHandlerContext.channel().close();

    }




    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        logger.error("client catch exception ");
        throwable.printStackTrace();
        channelHandlerContext.close();

    }
}
