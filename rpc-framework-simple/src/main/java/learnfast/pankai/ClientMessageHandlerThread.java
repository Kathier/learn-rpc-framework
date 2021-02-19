package learnfast.pankai;


import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.enumration.RpcResponseCode;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * @Author PanKai
 * @Description
 * @Date 21:27
 **/
public class ClientMessageHandlerThread implements Runnable{
    private   static  final Logger logger= LoggerFactory.getLogger(ClientMessageHandlerThread.class);
    private Socket socket;
    private Object service;
    public ClientMessageHandlerThread(Socket socket, Object service){
        this.socket=socket;
        this.service=service;
    }
    @SneakyThrows
    public void run() {
        try(ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());){
            RpcRequest rpcRequest= (RpcRequest) objectInputStream.readObject();
            Object result=invokeTargetMethod(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result));
            //用于刷新此流，并将任何缓冲输出的字节立即写入基础流。
            objectOutputStream.flush();
        }catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("occur exception:", e);
        }
    }

    private  Object invokeTargetMethod(RpcRequest rpcRequest) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> cls=Class.forName(rpcRequest.getInterfaceName());
        //判断类是否实现了对应的接口
        if(!cls.isAssignableFrom(service.getClass())){
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_CLASS);

        }
        Method method=service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParameterTypes());
        if(null == method){
            return  RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
        }
        return  method.invoke(service,rpcRequest.getParameters());
    }
}
