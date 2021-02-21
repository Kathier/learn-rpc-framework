package learnfast.pankai.transport.socket;

import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.enumration.RpcErrorMessageEnum;
import learnfast.pankai.enumration.RpcResponseCode;
import learnfast.pankai.exception.RpcException;
import learnfast.pankai.transport.RpcClient;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class SocketRpcClient implements RpcClient {
    private static final Logger logger= LoggerFactory.getLogger(SocketRpcClient.class);

    private  String host;
    private  int port;



    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        try {
            Socket socket = new Socket(host, port);

            //建立连接后获取输出流,发送消息
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(rpcRequest);
            //获取响应
            ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
            RpcResponse rpcResponse= (RpcResponse) objectInputStream.readObject();
            if(null==rpcResponse){
                logger.error("服务调用失败，serviceName:{}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE,
                        "InterfaceName: "+rpcRequest.getInterfaceName());
            }
            if(rpcResponse.getCode()==null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())){
                logger.error("调用服务失败，serviceName:{},rpcResponse:{}",
                        rpcRequest.getInterfaceName(),rpcResponse);
                throw  new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE,
                        "InterfaceName: "+rpcRequest.getInterfaceName());
            }
            return  rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;


    }
}
