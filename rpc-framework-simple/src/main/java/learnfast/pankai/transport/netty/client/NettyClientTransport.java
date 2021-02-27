package learnfast.pankai.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.registry.ServiceRegistry;
import learnfast.pankai.registry.ZKServiceRegistry;
import learnfast.pankai.serialize.KryoSerializer;
import learnfast.pankai.transport.ClientTransport;
import learnfast.pankai.transport.netty.codec.NettyKryoDecoder;
import learnfast.pankai.transport.netty.codec.NettyKryoEncoder;
import learnfast.pankai.util.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

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
public class NettyClientTransport implements ClientTransport {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientTransport.class);
    private ServiceRegistry serviceRegistry;
    public NettyClientTransport(){
        this.serviceRegistry= new ZKServiceRegistry();

    }


    /**
     * 发送消息到服务端
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    public Object sendRpcRequest(RpcRequest rpcRequest){
        //AtomicReference是作用是对”对象”进行原子操作。 提供了一种读和写都是原子性的对象引用变量。
        // 原子意味着多个线程试图改变同一个AtomicReference(例如比较和交换操作)将不会使得AtomicReference处于不一致的状态
        AtomicReference<Object> result=new AtomicReference<>(null);
        try {
            InetSocketAddress inetSocketAddress=serviceRegistry.lookUpService(rpcRequest.getInterfaceName());
            Channel channel=ChannelProvider.get(inetSocketAddress);
            if(channel.isActive()){
                channel.writeAndFlush(rpcRequest).addListener(future -> {
                    if(future.isSuccess()){
                        logger.info(String.format("client send message : %s",rpcRequest.toString()));

                    }else{
                        logger.error("send failed ",future.cause());
                    }
                });
                //阻塞等待，直到channel关闭
                channel.closeFuture().sync();
                AttributeKey<RpcResponse> key=AttributeKey.valueOf("RpcResponse"+rpcRequest.getRequestId());
                //将服务端返回的数据即rpcResponse对象取出
                //channel实现了AttributeMap接口；每个channel上的AttributeMap属于共享数据
                RpcResponse rpcResponse=channel.attr(key).get();
                logger.info("client get response from channel:{}",rpcResponse);
                //检验rpcResponse和rpcRequest
                RpcMessageChecker.check(rpcRequest,rpcResponse);
                result.set(rpcResponse.getData());
            }else {
                NettyClient.close();
                System.exit(0);
            }

        } catch (InterruptedException e) {
            logger.error("occur exception when connect server ",e);
        }
        return result.get();

    }

}
