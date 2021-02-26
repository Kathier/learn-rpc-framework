package learnfast.pankai.transport.netty.server;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
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
                RpcRequest rpcRequest= (RpcRequest) msg;
                String interfaceName=rpcRequest.getInterfaceName();

                //反射调用目标方法（客户端需要执行的方法）得到返回结果
                Object result=rpcRequestHandler.handle(rpcRequest);
                logger.info(String.format("server get result : %s",result.toString()));
                ChannelFuture channelFuture=ctx.writeAndFlush(RpcResponse.success(result,rpcRequest.getRequestId()));
                channelFuture.addListener(ChannelFutureListener.CLOSE);
            } finally {
                ReferenceCountUtil.release(msg);
            }
        });

    }

    @Override
    public  void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        logger.error("server catch exception",cause);
        cause.printStackTrace();
        ctx.close();
    }


}
