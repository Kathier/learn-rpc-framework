package learnfast.pankai;

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
    //创建线程池令服务端处理多个客户端的连接
    private ExecutorService threadPool;
    private static  final Logger logger=  LoggerFactory.getLogger(RpcServer.class);

    public RpcServer(){
        //线程池参数
        int corePoolSize =10;
        int maximumPoolSize = 100;
        long keepAliveTime=1;
        BlockingQueue<Runnable> workQueue=new ArrayBlockingQueue<Runnable>(100);
        ThreadFactory threadFactory= Executors.defaultThreadFactory();
        this.threadPool=new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,TimeUnit.MINUTES,workQueue,threadFactory);
    }

    /**
     * 服务端主动注册服务
     * @param service
     * @param port
     */
    public void register(Object service ,int port){
        try {
            ServerSocket server=new ServerSocket(port);
            logger.info("server starts...");
            Socket socket;
            //通过accept方法监听客户端请求
            while((socket=server.accept())!=null){
                logger.info("client connected");
                //工人线程（worker thread）会一次抓一件工作来处理，当没有工作可做时，工人线程会停下来等待新的工作过来
                threadPool.execute( new WorkerThread(socket,service));
            }
        } catch (IOException e) {

            logger.error("occur IOException",e);
        }


    }


}
