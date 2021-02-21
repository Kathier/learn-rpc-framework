package learnfast.pankai.registry;

/**
 * Created by PanKai on 2021/2/19 11:13
 * 服务注册中心接口：
 * 将服务的注册和启动分离
 * 接口包含两个方法：一个服务注册，一个获取服务
 * @Description
 **/
public interface ServiceRegistry {
    <T> void register(Object service);
    Object getService(String serviceName);
}
