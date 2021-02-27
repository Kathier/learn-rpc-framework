package learnfast.pankai.provider;

/**
 * Created by PanKai on 2021/2/22 21:05
 * 保存和提供服务实例对象，服务端使用
 * @Description
 **/
public interface ServiceProvider {
    /**
     * 保存服务提供对象和服务实例对象实现的接口的关系
     * @param service  服务实例对象
     * @param serviceClass 服务实例对象实现的接口类
     * @param <T> 服务接口的类型
     */
    <T> void addServiceProvider(T service,Class<T> serviceClass);

    /**
     * 获取服务提供者
     * @param serviceName
     * @return
     */
    Object getServiceProvider(String serviceName);

}
