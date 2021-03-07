package learnfast.pankai.registry;

import learnfast.pankai.extension.SPI;

import java.net.InetSocketAddress;

/**
 * Created by PanKai on 2021/2/27 19:19
 * 服务发现接口
 **/
@SPI
public interface ServiceDiscovery {
    /**
     * 查找服务
     * @param serviceName 服务名称
     * @return 提供服务的地址
     */
    InetSocketAddress lookupService(String serviceName);
}
