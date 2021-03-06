package learnfast.pankai.transport.netty.server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.factory.SingletonFactory;
import learnfast.pankai.provider.ServiceProvider;
import learnfast.pankai.provider.ServiceProviderImpl;
import learnfast.pankai.serialize.KryoSerializer;
import learnfast.pankai.transport.netty.codec.NettyKryoDecoder;
import learnfast.pankai.transport.netty.codec.NettyKryoEncoder;
import learnfast.pankai.transport.netty.codec.RpcMessageDecoder;
import learnfast.pankai.transport.netty.codec.RpcMessageEncoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;


/**
 * Created by PanKai on 2021/2/20 17:02
 * 服务端，接收客户端的请求并处理，根据客户端的消息调用相应的方法，然后将结果返回客户端
 * @Description
 **/
@Slf4j
@Component
public class NettyServer  {
    public static final int PORT = 9998;
    private  static  final Logger logger= LoggerFactory.getLogger(NettyServer.class);
    private final KryoSerializer kryoSerializer;

    public NettyServer(){
        kryoSerializer=new KryoSerializer();
    }
    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);

    public void registerService(Object service, String serviceName) {
        serviceProvider.publishService(service, serviceName);
    }

    public  void start() throws UnknownHostException {
        // bossGroup线程的机制是多路复用，虽然是一个线程但是可以监听多个新连接
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        EventLoopGroup workerGroup=new NioEventLoopGroup();
        String host= InetAddress.getLocalHost().getHostAddress();
        try {
            ServerBootstrap bootstrap=new ServerBootstrap();
            //初始化两个线程，一个负责处理新的连接，一个负责处理读写
            bootstrap.group(bossGroup,workerGroup).
                    channel(NioServerSocketChannel.class).
                    handler(new LoggingHandler(LogLevel.INFO)).
                    //ChannelInitializer用来进行设置出站解码器和入站编码器。
                    childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel)  {
                            // 30 秒之内没有收到客户端请求的话就关闭连接
                            socketChannel.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            //将接收到的字节流解码成RpcRequest对象
                            // pipeline管道，它为用户对于i/o内容的处理提供了链式的处理模式
                            socketChannel.pipeline().addLast(new RpcMessageEncoder());
                            //将rpcResponse 编码为字节数组传输
                            socketChannel.pipeline().addLast(new RpcMessageDecoder());
                            //NettyServerHandler 位于服务器端和客户端责任链的尾部，直接和 RpcServer 对象打交道，
                            // 而无需关心字节序列的情况
                            socketChannel.pipeline().addLast(new NettyServerHandler());
                        }
                    })
                    //设置tcp缓冲区
                    //是否启用Nagle算法，该算法将小的碎片数据连接成更大的报文来最小化所发送的报文的数量。
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    //Backlog主要是指当ServerSocket还没执行accept时，这个时候的请求会放在os层面的一个队列里，
                    // 这个队列的大小即为backlog值
                    .option(ChannelOption.SO_BACKLOG,128)
                    .option(ChannelOption.SO_KEEPALIVE,true); //是否使用TCP的心跳机制
            //绑定端口，同步等待绑定成功
            ChannelFuture channelFuture=bootstrap.bind(host,PORT).sync();
            //等待服务端监听端口关闭
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("occur exception when start server ",e);

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }

}
