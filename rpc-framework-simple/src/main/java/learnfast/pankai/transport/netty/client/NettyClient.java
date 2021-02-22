package learnfast.pankai.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.serialize.KryoSerializer;
import learnfast.pankai.transport.netty.codec.NettyKryoDecoder;
import learnfast.pankai.transport.netty.codec.NettyKryoEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by PanKai on 2021/2/22 16:21
 * 用于初始化和关闭Bootstrap对象
 * @Description
 **/
public class NettyClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private static final Bootstrap bootstrap;
    private static final EventLoopGroup evntLoopGroup;

    //初始化相关资源
    static {
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
    }

    private NettyClient() {

    }

    public static void close() {
        logger.info("call close method");
        evntLoopGroup.shutdownGracefully();
    }

    public  static  Bootstrap initializeBootstrap(){
        return bootstrap;
    }

}
