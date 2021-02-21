package learnfast.pankai.transport;

import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.enumration.RpcResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by PanKai on 2021/2/19 10:43
 * @Description 通过反射进行方法调用
 **/
public class RpcRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);

    public Object handle(RpcRequest rpcRequest,Object service){
        Object result =null;
        try {
            result=invokeTargetMethod(rpcRequest,service);
            logger.info("service:{} successfully invoke method :{}"
                    ,rpcRequest.getInterfaceName(),rpcRequest.getMethodName());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException  e) {
            logger.error("occur exception ",e);
        }
        return result;
    }

    private  Object invokeTargetMethod(RpcRequest rpcRequest,Object service) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method method=service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParameterTypes());
        if(null == method){
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
        }
        return method.invoke(service,rpcRequest.getParameters());
    }

}
