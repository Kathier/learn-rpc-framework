package learnfast.pankai.provider;

/**
 * Created by PanKai on 2021/2/22 21:05
 * 保存和提供服务实例对象，服务端使用
 * @Description
 **/
public interface ServiceProvider {
    /**
     * 保存服务提供者
     * @param service
     * @param <T>
     */
    <T> void addServiceProvider(T service);

    /**
     * 获取服务提供者
     * @param serviceName
     * @return
     */
    Object getServiceProvider(String serviceName);

}
