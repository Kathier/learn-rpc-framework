package learnfast.pankai.util;
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.enumration.RpcErrorMessageEnum;
import learnfast.pankai.enumration.RpcResponseCode;
import learnfast.pankai.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by PanKai on 2021/2/22 10:37
 *
 * @Description
 **/
public class RpcMessageChecker {
    private  static  final Logger logger= LoggerFactory.getLogger(RpcMessageChecker.class);
    public   static  final  String INTERFACE_NAME="interfaceName";
    public RpcMessageChecker(){

    }
    public static void check(RpcRequest rpcRequest, RpcResponse rpcResponse){
        if(rpcResponse==null){
            logger.error("调用服务失败，rpcResponse为空，serviceName: {}",rpcRequest.getInterfaceName());
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE,INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }
        if(!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())){
            logger.error("服务调用失败：rpcRequest和rpcResponse 不匹配，serviceName :{} ,rpcResponse :{}",rpcRequest.getInterfaceName(),rpcResponse);
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE,INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }
        if(rpcResponse.getCode()==null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())){
            logger.error("服务调用失败：serviceName :{} ,rpcResponse :{}",rpcRequest.getInterfaceName(),rpcResponse);
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE,INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }
    }
}
