package learnfast.pankai.transport.netty;

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
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.serialize.KryoSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by PanKai on 2021/2/20 17:02
 * 开启服务端，接收客户端的请求并处理
 * @Description
 **/
public class NettyRpcServer {
    private  static  final Logger logger= LoggerFactory.getLogger(NettyRpcServer.class);
    private  final   int port;
    private KryoSerializer kryoSerializer;
    public  NettyRpcServer(int port){
        this.port=port;
        kryoSerializer=new KryoSerializer();
    }
    public  void run(){
        // bossGroup线程的机制是多路复用，虽然是一个线程但是可以监听多个新连接
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        EventLoopGroup workerGroup=new NioEventLoopGroup();
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
                            //将接收到的字节流解码成RpcRequest对象
                            // pipeline管道，它为用户对于i/o内容的处理提供了链式的处理模式
                            socketChannel.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcRequest.class));
                            //将rpcResponse 编码为字节数组传输
                            socketChannel.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcResponse.class));
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
            ChannelFuture channelFuture=bootstrap.bind(port).sync();
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
