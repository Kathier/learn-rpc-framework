package learnfast.pankai.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.extension.ExtensionLoader;
import learnfast.pankai.factory.SingletonFactory;
import learnfast.pankai.registry.ServiceDiscovery;
import learnfast.pankai.serialize.KryoSerializer;
import learnfast.pankai.transport.RpcRequestTransport;
import learnfast.pankai.transport.netty.codec.NettyKryoDecoder;
import learnfast.pankai.transport.netty.codec.NettyKryoEncoder;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Created by PanKai on 2021/2/22 16:21
 * 用于初始化和关闭Bootstrap对象
 * @Description
 **/
public class NettyClient implements RpcRequestTransport {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private  final Bootstrap bootstrap;
    private  final EventLoopGroup evntLoopGroup;
    private  final ServiceDiscovery serviceDiscovery;
    private  final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;
    public NettyClient() {
        
        //初始化相关资源

        //Bootstrap用来连接远程主机，有1个EventLoopGroup
        bootstrap = new Bootstrap();
        //EventLoop 用于处理 Channel 的 I/O 操作。一个单一的 EventLoop通常会处理多个 Channel 事件。
        // 一个 EventLoopGroup 可以含有多于一个的 EventLoop 和 提供了一种迭代用于检索清单中的下一个。
        evntLoopGroup = new NioEventLoopGroup();
        KryoSerializer kryoSerializer = new KryoSerializer();
        bootstrap.group(evntLoopGroup).
                channel(NioSocketChannel.class).
                //设置连接超时时间
                        option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000).
                //开启TCP底层心跳机制
                        option(ChannelOption.SO_KEEPALIVE, true).
                ////TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                        option(ChannelOption.TCP_NODELAY, true).
                handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //自定义序列化编解码器
                        //ByteBuf->RpcResponse
                        socketChannel.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                        socketChannel.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));
                        socketChannel.pipeline().addLast(new NettyClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    public  void close() {
        logger.info("call close method");
        evntLoopGroup.shutdownGracefully();
    }

    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.info("客户端连接成功!");
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    /**
     * 发送消息到服务端
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    @Override
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
