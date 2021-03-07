package learnfast.pankai.registry;

import learnfast.pankai.extension.SPI;

import java.net.InetSocketAddress;

/**
 * Created by PanKai on 2021/2/22 21:18
 * 服务注册中心接口
 * @Description
 **/
@SPI
public interface ServiceRegistry {
    /**
     * 注册服务
     * @param serviceName 服务名
     * @param inetSocketAddress 提供服务的地址
     */
    void registerService(String serviceName, InetSocketAddress inetSocketAddress);



}
