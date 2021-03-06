package learnfast.pankai.provider;

import learnfast.pankai.enumration.RpcErrorMessageEnum;
import learnfast.pankai.exception.RpcException;
import learnfast.pankai.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by PanKai on 2021/2/19 11:04
 * 默认的服务注册中心实现,通过map保存服务信息，可以改进为使用zookeeper
 * @Description
 **/
public class ServiceProviderImpl implements ServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderImpl.class);

    /**
     * 定义一个map存储接口和它的多个实现
     * key:service/Interface name
     * value:service
     * 将包含注册信息的 serviceMap 和 registeredService 都改成了 static ，
     * 这样就能保证全局唯一的注册信息，并且在创建 RpcServer 时也就不需要传入了
     */
    private static  final Map<String,Object> serviceMap=new ConcurrentHashMap<>();
    private  static  final Set<String> registeredService=ConcurrentHashMap.newKeySet();

    /**
     * 修改为扫描注解注册
     * 将该对象的所有实现了的接口注册进去
     * @param service
     * @param <T>
     */

    @Override
    public <T> void addServiceProvider(T service,Class<T> serviceClass) {
        String serviceName=serviceClass.getCanonicalName();
        if(registeredService.contains(serviceName)){
            return ;
        }

        //获取service实现的所有接口

        registeredService.add(serviceName);
        serviceMap.put(serviceName,service);
        logger.info("add service :{}  and interfaces :{} ",service,service.getClass().getInterfaces());
    }

    @Override
    public Object getServiceProvider(String serviceName) {
        Object service=serviceMap.get(serviceName);
        if(service==null){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service ;
    }
}
