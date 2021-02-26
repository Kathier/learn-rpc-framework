package learnfast.pankai.transport.socket;

import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.provider.ServiceProviderImpl;
import learnfast.pankai.registry.ServiceRegistry;
import learnfast.pankai.handler.RpcRequestHandler;
import learnfast.pankai.registry.ZKServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by PanKai on 2021/2/19 11:01
 *
 * @Description
 **/
public class SocketRpcRequestHandlerRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SocketRpcRequestHandlerRunnable.class);
    private Socket socket;
    private static  RpcRequestHandler rpcRequestHandler;

    //静态代码块随着类的加载而执行，而且只执行一次
    static {
        rpcRequestHandler=new RpcRequestHandler();
    }
    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;

    }
    @Override
    public void run() {
        logger.info(String.format("server handle message from client by thread : %s",Thread.currentThread().getName()));
        try(ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());){
            RpcRequest rpcRequest= (RpcRequest) objectInputStream.readObject();
            Object result=rpcRequestHandler.handle(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result,rpcRequest.getRequestId()));
            //用于刷新此流，并将任何缓冲输出的字节立即写入基础流。
            objectOutputStream.flush();
        }catch (IOException | ClassNotFoundException  e) {
            logger.error("occur exception:", e);
        }
    }
}
