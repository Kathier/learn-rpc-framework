package learnfast.pankai.transport;

import learnfast.pankai.dto.RpcRequest;

/**
 * Created by PanKai on 2021/2/19 20:32
 * 发送消息到服务端
 *  RpcRequest 消息体
 *  return 服务端返回的数据
 * @Description
 **/
public interface ClientTransport {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
