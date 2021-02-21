package learnfast.pankai.transport;

import learnfast.pankai.dto.RpcRequest;

/**
 * Created by PanKai on 2021/2/19 20:32
 *
 * @Description
 **/
public interface RpcClient {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
