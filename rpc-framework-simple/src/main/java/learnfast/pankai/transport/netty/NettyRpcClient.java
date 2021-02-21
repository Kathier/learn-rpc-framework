package learnfast.pankai.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.serialize.KryoSerializer;
import learnfast.pankai.transport.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class NettyRpcClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyRpcClient.class);
    private String host;
    private int port;
    private static  final  Bootstrap b;
    public  NettyRpcClient(String host,int port){
        this.host=host;
        this.port=port;
    }
    //初始化相关资源
    static {
        //EventLoop 用于处理 Channel 的 I/O 操作。一个单一的 EventLoop通常会处理多个 Channel 事件。
        // 一个 EventLoopGroup 可以含有多于一个的 EventLoop 和 提供了一种迭代用于检索清单中的下一个。
        EventLoopGroup eventLoopGroup=new NioEventLoopGroup();
        //Bootstrap用来连接远程主机，有1个EventLoopGroup
        b=new Bootstrap();
        KryoSerializer kryoSerializer=new KryoSerializer();
        b.group(eventLoopGroup).
                channel(NioSocketChannel.class).
                option(ChannelOption.SO_KEEPALIVE,true).
                handler(new ChannelInitializer< SocketChannel >(){
                    /**
                     * 责任链模式，责任链上有多个处理器，每个处理器都会对数据进行加工，并将处理后的数据传给下一个处理器。
                     * 代码中的 NettyKryoEncoder、NettyKryoDecoder和NettyClientHandler 分别就是编码器，解码器和数据处理器。
                     * 因为数据从外部传入时需要解码，而传出时需要编码，类似计算机网络的分层模型，
                     * 每一层向下层传递数据时都要加上该层的信息，而向上层传递时则需要对本层信息进行解码
                     * @param socketChannel
                     * @throws Exception
                     */
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //自定义序列化编解码器
                        //ByteBuf->RpcResponse
                        socketChannel.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcResponse.class));
                        //RpcRequest->byteBuf
                        socketChannel.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcRequest.class));

                        socketChannel.pipeline().addLast(new NettyClientHandler());
                    }
                });



    }

    /**
     * 发送消息到服务端
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    public Object sendRpcRequest(RpcRequest rpcRequest){
        try {//通过bootstrap对象连接服务端，sync开启监听器获取I/O事件结果
            //ChannelFuture执行异步操作时使用;事件监听者能够确认这个操作是否成功或者是错误
            ChannelFuture f=b.connect(host,port).sync();

            logger.info("client connect {}",host+":"+port);
            //通过channel向服务端发送rpcRequest;
            // Channel相当于一个可以“打开”或“关闭”，“连接”或“断开”和作为传入和传出数据的运输工具。
            Channel futureChannel=f.channel();
            if(futureChannel!=null){                                //异步的返回结果
                futureChannel.writeAndFlush(rpcRequest).addListener(future -> {
                    if(future.isSuccess()){
                        logger.info(String.format("client send message : %s",rpcRequest.toString()));

                    }else{
                        logger.error("send failed ",future.cause());
                    }
                });
            }
            //阻塞等待，直到channel关闭
            futureChannel.closeFuture().sync();
            AttributeKey<RpcResponse> key=AttributeKey.valueOf("RpcResponse");
            //将服务端返回的数据即rpcResponse对象取出
            //channel实现了AttributeMap接口；每个channel上的AttributeMap属于共享数据
            RpcResponse rpcResponse=futureChannel.attr(key).get();
            return rpcResponse.getData();
        } catch (InterruptedException e) {
            logger.error("occur exception when connect server ",e);
        }
        return null;

    }

}
