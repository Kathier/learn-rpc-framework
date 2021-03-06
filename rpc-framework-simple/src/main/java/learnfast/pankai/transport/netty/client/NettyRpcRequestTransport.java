package learnfast.pankai.transport.netty.client;

import io.netty.channel.*;
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.extension.ExtensionLoader;
import learnfast.pankai.factory.SingletonFactory;
import learnfast.pankai.registry.ServiceDiscovery;
import learnfast.pankai.transport.RpcRequestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Created by PanKai on 2021/2/19 21:34
 * 客户端中主要有一个向服务端发送消息的sendMessage()方法，通过这个方法可以将消息
 * 即RpcRequest对象发送到服务端，并且可以同步获取到服务端返回的结果即RpcResponse
 * @Description
 * 1.创建一个 Bootstrap
 *
 * 2.使用 NioEventLoopGroup 允许非阻塞模式（NIO）
 *
 * 3.指定 ChannelInitializer 将给每个接受的连接调用
 *
 * 4.添加的 ChannelInboundHandlerAdapter() 接收事件并进行处理
 *
 * 5.写信息到客户端，并添加 ChannelFutureListener 当一旦消息写入就关闭连接
 *
 * 6.绑定服务器来接受连接
 *
 * 7.释放所有资源
 **/
public class NettyRpcRequestTransport implements RpcRequestTransport {
    private static final Logger logger = LoggerFactory.getLogger(NettyRpcRequestTransport.class);
    private final ServiceDiscovery serviceDiscovery;
    private  final  UnprocessedRequests unprocessedRequests;
    public NettyRpcRequestTransport(){
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequests= SingletonFactory.getInstance(UnprocessedRequests.class);
    }


    /**
     * 发送消息到服务端
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    public Object sendRpcRequest(RpcRequest rpcRequest){
        //构建返回值
        CompletableFuture<RpcResponse<Object>> resultFuture=new CompletableFuture<>();

        InetSocketAddress inetSocketAddress=serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        Channel channel=ChannelProvider.get(inetSocketAddress);
        if(channel!=null && channel.isActive()){
            //放入未处理的请求
            unprocessedRequests.put(rpcRequest.getRequestId(),resultFuture);
            channel.writeAndFlush(rpcRequest).addListener(  (ChannelFutureListener) future -> {
                if(future.isSuccess()){
                    logger.info(String.format("client send message : %s",rpcRequest.toString()));

                }else{
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    logger.error("send failed ",future.cause());
                }
            });

        }else {
            throw new IllegalStateException();
        }
        return resultFuture;

    }

}
