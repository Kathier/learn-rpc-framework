package learnfast.pankai;

import learnfast.pankai.dto.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


/**
 * 2021/2/14
 * 客户端发送rpc请求，通过socket实现网络传输
 * 套接字即ip地址+端口号
 * 客户端实现功能如下：
 * 创建socket对象并连接指定的服务器IP和端口号
 * 连接建立后通过输入流向服务器发送请求消息
 * 通过输出流向客户端发送响应消息
 * 关闭相关资源
 */
public class RpcClient {
    public static final Logger logger= LoggerFactory.getLogger(RpcClient.class);

    public Object sendRpcRequest(RpcRequest rpcRequest, String host, int port){

        try {
            Socket socket = new Socket(host, port);

            //建立连接后获取输出流,发送消息
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            //获取响应
            ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
            //ObjectInputStream 反序列化流，将之前使用 ObjectOutputStream 序列化的原始数据恢复为对象，以流的方式读取对象。
            return objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

}
