package learnfast.pankai.transport.socket;

import learnfast.pankai.handler.RpcRequestHandler;
import learnfast.pankai.provider.ServiceProvider;
import learnfast.pankai.provider.ServiceProviderImpl;
import learnfast.pankai.registry.ServiceRegistry;
import learnfast.pankai.registry.ZKServiceRegistry;
import learnfast.pankai.util.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.*;



/**
 * @Author PanKai
 * @Description
 * 服务端实现功能
 * 创建serverSocket对象并绑定IP地址和端口号
 * 通过accept方法监听客户端请求和
 * 连接建立后通过输入流接收客户端发送的请求
 * 通过输出流向客户端发送响应信息
 * 关闭资源
 * @Date 2021/2/15
 **/
public class SocketRpcServer {

    private static  final Logger logger=  LoggerFactory.getLogger(SocketRpcServer.class);
    private final  ExecutorService threadPool;
    private  RpcRequestHandler rpcRequestHandler=new RpcRequestHandler();
    private  final  String host;
    private final  int port;
    private  final ServiceRegistry serviceRegistry;
    private  final ServiceProvider serviceProvider;
    public SocketRpcServer(String host,int port){
        this.host=host;
        this.port=port;
        threadPool= ThreadPoolFactory.createDefautThreadPool("socket-server-rpc-pool");
        serviceRegistry=new ZKServiceRegistry();
        serviceProvider=new ServiceProviderImpl();
    }

    public <T> void publishService(T service,Class<T> serviceClass){
        serviceProvider.addServiceProvider(service,serviceClass);
        serviceRegistry.registerService(serviceClass.getCanonicalName(),new InetSocketAddress(host,port));
        start();
    }
    /**
     * 服务启动
     */
    private void start(){

        try ( ServerSocket server=new ServerSocket();){
            server.bind(new InetSocketAddress(host,port));
            logger.info("server starts...");
            Socket socket;
            //通过accept方法监听客户端请求
            while((socket=server.accept())!=null){
                logger.info("client connected");
                threadPool.execute( new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {

            logger.error("occur IOException",e);
        }


    }


}
