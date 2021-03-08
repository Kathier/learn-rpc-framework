package learnfast.pankai.transport.netty.server;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import learnfast.pankai.constants.RpcConstants;
import learnfast.pankai.dto.RpcMessage;
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.enumration.RpcMessageTypeEnum;
import learnfast.pankai.enumration.RpcResponseCode;
import learnfast.pankai.enumration.SerializationTypeEnum;
import learnfast.pankai.handler.RpcRequestHandler;
import learnfast.pankai.util.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * Created by PanKai on 2021/2/20 17:43
 *  自定义服务端的channelHandler来处理客户端发来的数据
 * 用于接收RpcRequest,并且执行调用，将调用结果封装成rpcResponse对象返回
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 *  channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 * @Description
 **/
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static  final  RpcRequestHandler rpcRequestHandler;
    private  static  final  String THREAD_NAME_PREFIX="netty-server-rpc-pool";
    private  static ExecutorService threadPool;

    static {
        rpcRequestHandler=new RpcRequestHandler();
        threadPool= ThreadPoolFactory.createDefautThreadPool(THREAD_NAME_PREFIX);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        //当我们调用ThreadPoolExecutor中的Execute方法时，
        // 线程池会对当前自身状态做出判断来决定是否创建新的worker来立即执行task，或者是将task放置在workQueue队列中
        threadPool.execute(()->{
            logger.info(String.format("server handle message from client by thread : %s",Thread.currentThread().getName()));
            try {
                logger.info(String.format("server receieve msg :%s ",msg));
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KRYO.getCode());

                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                }else{
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    //反射调用目标方法（客户端需要执行的方法）得到返回结果
                    Object result=rpcRequestHandler.handle(rpcRequest);
                    logger.info(String.format("server get result : %s",result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if(ctx.channel().isActive() && ctx.channel().isWritable()){
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    }else{
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCode.FAIL);
                        rpcMessage.setData(rpcResponse);
                        logger.error("not writeable now ,message droped");
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } finally {
                //释放byteBuf，避免内存泄漏
                ReferenceCountUtil.release(msg);
            }
        });

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
            if (state == IdleState.READER_IDLE) {
                logger.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public  void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        logger.error("server catch exception",cause);
        cause.printStackTrace();
        ctx.close();
    }


}
