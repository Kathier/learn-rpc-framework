package learnfast.pankai.transport.socket;

import learnfast.pankai.transport.RpcRequestHandler;
import learnfast.pankai.util.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    private ExecutorService threadPool;
    private  RpcRequestHandler rpcRequestHandler=new RpcRequestHandler();
    public SocketRpcServer(){
       threadPool= ThreadPoolFactory.createDefautThreadPool("socket-server-rpc-pool");
    }

    /**
     * 服务启动
     * @param port
     */
    public void start(int port){

        try {
            ServerSocket server=new ServerSocket(port);
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
