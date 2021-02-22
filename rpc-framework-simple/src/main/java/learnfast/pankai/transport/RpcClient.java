package learnfast.pankai.transport;

import learnfast.pankai.dto.RpcRequest;

/**
 * Created by PanKai on 2021/2/19 20:32
 * 实现了RpcClient接口的类需要具有发送rpcRequest对象的功能
 * @Description
 **/
public interface RpcClient {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
