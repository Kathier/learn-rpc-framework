package learnfast.pankai.registry;

import learnfast.pankai.enumration.RpcErrorMessageEnum;
import learnfast.pankai.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by PanKai on 2021/2/19 11:04
 *
 * @Description
 **/
public class DefaultServiceRegistry implements ServiceRegistry{
    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceRegistry.class);

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
    public synchronized  <T> void register(Object service) {
        String serviceName=service.getClass().getCanonicalName();
        if(registeredService.contains(serviceName)){
            return ;
        }

        //获取service实现的所有接口
        Class [] interfaces=service.getClass().getInterfaces();
        if(interfaces.length==0){
            throw  new RpcException(RpcErrorMessageEnum.SERVICE_NOT_IMPLEMENT_ANY_INTERFACES);
        }
        for (Class i: interfaces){
            serviceMap.put(i.getCanonicalName(),service);
        }
        registeredService.add(serviceName);
        logger.info("add service :{}  and interfaces :{} ",service,service.getClass().getInterfaces());

    }

    @Override
    public Object getService(String serviceName) {
        Object service=serviceMap.get(serviceName);
        if(service==null){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service ;
    }
}
