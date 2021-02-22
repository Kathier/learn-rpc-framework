package learnfast.pankai.transport;
import learnfast.pankai.dto.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @Author PanKai
 * 动态代理类，当动态代理对象调用一个方法的时候，实际调用的是下面的invoke方法
 * @Description 由于在客户端这一侧我们并没有接口的具体实现类，就没有办法直接生成实例对象。
 * 这时，我们可以通过动态代理的方式生成实例，并且调用方法时生成需要的RpcRequest对象并且发送给服务端。
 * @Date 2021/2/18/10:06
 **/
public class RpcClientProxy implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);
    /**
     * 用于发送请求给服务端，有socket和netty两种实现方式
     * @param rpcClient
     */
    private ClientTransport clientTransport;

    public RpcClientProxy(ClientTransport clientTransport){
      this.clientTransport = clientTransport;
    }
    //通过Proxy.newProxyInstance()方法获取某个类的代理对象
    @SuppressWarnings("unchecked") //告诉编译器忽略 unchecked 警告信息
    public <T> T getProxy(Class <T> clazz){

        return (T) Proxy.newProxyInstance(
                //传入classLoader,传入待实现的接口，传入处理调用方法的InvocationHandler
                clazz.getClassLoader(),new Class[]{clazz},RpcClientProxy.this);
    }

    /**
     * 动态代理对象调用其方法的时候，实际是调用实现InvocationHandler重写的invoke方法
     * 代理对象就是上面getProxy方法获取的对象
     * @param proxy  动态生成的代理类
     * @param method 与代理类调用的方法相对应
     * @param args   当前method方法的参数
     * @return
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)  {
        logger.info("call invoke method and the method is :{}",method.getName());
        RpcRequest rpcRequest=RpcRequest.builder().methodName(method.getName()).
                parameters(args).
                interfaceName(method.getDeclaringClass().getName()).
                parameterTypes(method.getParameterTypes()).
                requestId(UUID.randomUUID().toString()).
                build();
        return clientTransport.sendRpcRequest(rpcRequest);
    }
}
