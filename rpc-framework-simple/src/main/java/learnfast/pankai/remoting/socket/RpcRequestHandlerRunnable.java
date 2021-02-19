package learnfast.pankai.remoting.socket;

import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.registry.ServiceRegistry;
import learnfast.pankai.remoting.RpcRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

/**
 * Created by PanKai on 2021/2/19 11:01
 *
 * @Description
 **/
public class RpcRequestHandlerRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandlerRunnable.class);
    private Socket socket;
    private RpcRequestHandler rpcRequestHandler;
    private ServiceRegistry serviceRegistry;
    public RpcRequestHandlerRunnable(Socket socket, RpcRequestHandler rpcRequestHandler, ServiceRegistry serviceRegistry) {
        this.socket = socket;
        this.rpcRequestHandler=rpcRequestHandler;
        this.serviceRegistry = serviceRegistry;
    }
    @Override
    public void run() {
        try(ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());){
            RpcRequest rpcRequest= (RpcRequest) objectInputStream.readObject();
            String serviceName= rpcRequest.getInterfaceName();
            Object service=serviceRegistry.getService(serviceName);
            Object result=rpcRequestHandler.handle(rpcRequest,service);
            objectOutputStream.writeObject(RpcResponse.success(result));
            //用于刷新此流，并将任何缓冲输出的字节立即写入基础流。
            objectOutputStream.flush();
        }catch (IOException | ClassNotFoundException  e) {
            logger.error("occur exception:", e);
        }
    }
}
