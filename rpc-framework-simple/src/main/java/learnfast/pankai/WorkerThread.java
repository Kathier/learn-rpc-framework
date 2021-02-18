package learnfast.pankai;


import learnfast.pankai.dto.RpcRequest;
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
public class WorkerThread implements Runnable{
    private   static  final Logger logger= LoggerFactory.getLogger(WorkerThread.class);
    private Socket socket;
    private Object service;
    public  WorkerThread(Socket socket,Object service){
        this.socket=socket;
        this.service=service;
    }
    @SneakyThrows
    public void run() {
        try(ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());){
            RpcRequest rpcRequest= (RpcRequest) objectInputStream.readObject();
            Method method=service.getClass().getMethod(
                    rpcRequest.getMethodName(),rpcRequest.getParameterTypes());
            Object result=method.invoke(service,rpcRequest.getParameters());
            objectOutputStream.writeObject(result);
            //用于刷新此流，并将任何缓冲输出的字节立即写入基础流。
            objectOutputStream.flush();
        }catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("occur exception:", e);
        }



    }
}
