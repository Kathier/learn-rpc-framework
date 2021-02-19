package learnfast.pankai.remoting.socket;

import learnfast.pankai.enumration.RpcErrorMessageEnum;
import learnfast.pankai.exception.RpcException;
import learnfast.pankai.registry.ServiceRegistry;
import learnfast.pankai.remoting.RpcRequestHandler;
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
public class RpcServer {


    private static  final Logger logger=  LoggerFactory.getLogger(RpcServer.class);

    /**
     * 线程池参数
     */
    private  static  final  int  CORE_POOL_SIZE=10;
    private static  final  int MAXINUM_POOL_SIZE=100;
    private  static  final int KEEP_ALIVE_TIME=1;
    private  static   final  int BLOCKING_QUEUE_CAPCITY=100;

    private ExecutorService threadPool;
    private  final ServiceRegistry serviceRegistry;
    private  RpcRequestHandler rpcRequestHandler=new RpcRequestHandler();
    public RpcServer(ServiceRegistry serviceRegistry){
        this.serviceRegistry = serviceRegistry;
        BlockingQueue<Runnable> workQueue=new ArrayBlockingQueue<Runnable>(BLOCKING_QUEUE_CAPCITY);
        ThreadFactory threadFactory= Executors.defaultThreadFactory();
        this.threadPool=new ThreadPoolExecutor(CORE_POOL_SIZE,MAXINUM_POOL_SIZE,KEEP_ALIVE_TIME,TimeUnit.MINUTES,workQueue,threadFactory);
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
                threadPool.execute( new RpcRequestHandlerRunnable(socket,rpcRequestHandler,serviceRegistry));
            }
            threadPool.shutdown();
        } catch (IOException e) {

            logger.error("occur IOException",e);
        }


    }


}
