package learnfast.pankai.transport.netty.client;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.factory.SingletonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by PanKai on 2021/2/20 11:46
 * 自定义客户端channnelHandler来处理服务端发来的数据
 * @Description
 **/
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    private final UnprocessedRequests unprocessedRequests;
    public NettyClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);

    }
    //读取服务端发来的信息
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        logger.info(String.format("client recieve msg : %s",msg));
        try {

            RpcResponse rpcResponse = (RpcResponse) msg;
            unprocessedRequests.complete(rpcResponse);
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    //处理客户端消息发生异常的时候调用
    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
        logger.error("client catch exception ",throwable);
        throwable.printStackTrace();
        channelHandlerContext.close();

    }
}
