package learnfast.pankai.transport.socket;

import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.exception.RpcException;
import learnfast.pankai.registry.ServiceDiscovery;
import learnfast.pankai.registry.ServiceRegistry;
import learnfast.pankai.registry.ZKServiceDiscovery;
import learnfast.pankai.registry.ZKServiceRegistry;
import learnfast.pankai.transport.ClientTransport;
import learnfast.pankai.util.RpcMessageChecker;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
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
@AllArgsConstructor
public class SocketRpcClient implements ClientTransport {
    private static final Logger logger= LoggerFactory.getLogger(SocketRpcClient.class);
    private final ServiceDiscovery serviceDiscovery;
    public SocketRpcClient(){
        serviceDiscovery=new ZKServiceDiscovery();
    }



    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress=serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        try (Socket socket = new Socket()){
            socket.connect(inetSocketAddress);
            //建立连接后获取输出流,发送消息
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
            //通过输出流发送数据到服务端
            objectOutputStream.writeObject(rpcRequest);
            //获取响应
            ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
            //从输入流中读取出rpcResponse
            RpcResponse rpcResponse= (RpcResponse) objectInputStream.readObject();
            //校验rpcRequest和rpcResponse
            RpcMessageChecker.check(rpcRequest,rpcResponse);
            return  rpcResponse.getData();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("occur exception when sendRpcRequest",e);
            throw new RpcException("发送服务调用请求失败",e);
        }
    }
}
