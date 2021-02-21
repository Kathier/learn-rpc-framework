package learnfast.pankai.transport.netty;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.registry.DefaultServiceRegistry;
import learnfast.pankai.registry.ServiceRegistry;
import learnfast.pankai.transport.RpcRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by PanKai on 2021/2/20 17:43
 * 用于接收RpcRequest,并且执行调用，将调用结果封装成rpcResponse对象返回
 * @Description
 **/
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static  RpcRequestHandler rpcRequestHandler;
    private  static ServiceRegistry serviceRegistry;
    static {
        rpcRequestHandler=new RpcRequestHandler();
        serviceRegistry=new DefaultServiceRegistry();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg){
        try {
            RpcRequest rpcRequest= (RpcRequest) msg;
            logger.info(String.format("server receieve msg :%s ",rpcRequest));
            String interfaceName=rpcRequest.getInterfaceName();
            //获取服务
            Object service=serviceRegistry.getService(interfaceName);
            //反射调用方法得到返回结果
            Object result=rpcRequestHandler.handle(rpcRequest,service);
            logger.info(String.format("server get result : %s",result.toString()));
            ChannelFuture channelFuture=ctx.writeAndFlush(RpcResponse.success(result));
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public  void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        logger.error("server catch exception",cause);
        cause.printStackTrace();
        ctx.close();
    }


}
