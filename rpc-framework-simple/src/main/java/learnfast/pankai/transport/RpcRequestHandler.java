package learnfast.pankai.transport;

import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.enumration.RpcResponseCode;
import learnfast.pankai.registry.DefaultServiceRegistry;
import learnfast.pankai.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.attribute.standard.RequestingUserName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by PanKai on 2021/2/19 10:43
 * 进行过程调用的处理器
 * @Description 通过反射进行方法调用
 **/
public class RpcRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);
    private  static final ServiceRegistry serviceRegistry;
    static {
        serviceRegistry=new DefaultServiceRegistry();
    }

    public Object handle(RpcRequest rpcRequest){
        Object result =null;
        //通过注册中心获取到目标类（客户端需要调用类）
        Object service=serviceRegistry.getService(rpcRequest.getInterfaceName());
        try {
            result=invokeTargetMethod(rpcRequest,service);
            logger.info("service:{} successfully invoke method :{}"
                    ,rpcRequest.getInterfaceName(),rpcRequest.getMethodName());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException  e) {
            logger.error("occur exception ",e);
        }
        return result;
    }

    /**
     * 根据 rpcRequest 、service 对象 反射调用对应的方法并返回结果
     */

    private  Object invokeTargetMethod(RpcRequest rpcRequest,Object service) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method method=service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParameterTypes());
        if(null == method){
            return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
        }
        return method.invoke(service,rpcRequest.getParameters());
    }

}
