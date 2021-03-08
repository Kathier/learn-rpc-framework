package learnfast.pankai.transport.netty.client;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import learnfast.pankai.constants.RpcConstants;
import learnfast.pankai.dto.RpcMessage;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.enumration.SerializationTypeEnum;
import learnfast.pankai.factory.SingletonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;

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
            RpcMessage tmp = (RpcMessage) msg;
            byte messageType = tmp.getMessageType();
            if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                logger.info("heart [{}]", tmp.getData());
            } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
            unprocessedRequests.complete(rpcResponse);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    /**
     * 心跳检查
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                logger.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = ChannelProvider.get((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                //设置序列化实现方式
                rpcMessage.setCodec(SerializationTypeEnum.KRYO.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
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
