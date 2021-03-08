package learnfast.pankai.handler;

import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.enumration.RpcResponseCode;
import learnfast.pankai.exception.RpcException;
import learnfast.pankai.provider.ServiceProvider;
import learnfast.pankai.provider.ServiceProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by PanKai on 2021/2/19 10:43
 * 进行过程调用的处理器
 * @Description 通过反射进行方法调用
 **/
public class RpcRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);
    private  static final ServiceProvider serviceProvider;
    static {
        serviceProvider=new ServiceProviderImpl();
    }

    /**
     *  处理rpcRequest，调用对应的处理方法，然后返回方法执行结果
     * @param rpcRequest
     * @return
     */
    public Object handle(RpcRequest rpcRequest){
        Object result =null;
        //通过注册中心获取到目标类（客户端需要调用类）
        Object service=serviceProvider.getService(rpcRequest.getInterfaceName());
        result=invokeTargetMethod(rpcRequest,service);
        logger.info("service:{} successfully invoke method :{}"
                ,rpcRequest.getInterfaceName(),rpcRequest.getMethodName());

        return result;
    }

    /**
     * 根据 rpcRequest 、service 对象 反射调用对应的方法并返回结果
     */

    private  Object invokeTargetMethod(RpcRequest rpcRequest,Object service) {
        Object result;
        try {
            Method method=service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParameterTypes());
            if(null == method){
                return RpcResponse.fail(RpcResponseCode.NOT_FOUND_METHOD);
            }
            result= method.invoke(service,rpcRequest.getParameters());
            return result;
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }

    }

}
