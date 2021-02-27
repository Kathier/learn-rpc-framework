package learnfast.pankai.registry;

import learnfast.pankai.util.CuratorHelper;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by PanKai on 2021/2/27 19:22
 **/
public class ZKServiceDiscovery implements ServiceDiscovery{

    private static final Logger logger = LoggerFactory.getLogger(ZKServiceDiscovery.class);
    private  final CuratorFramework zkClient;
    public  ZKServiceDiscovery(){
        this.zkClient=CuratorHelper.getZKClient();
        zkClient.start();
    }


    @Override
    public InetSocketAddress lookupService(String serviceName) {
            //负载均衡，这里直接去了找到的第一个服务的地址
            String serviceAddress=CuratorHelper.getChildrenNodes(zkClient,serviceName).get(0);
            logger.info("成功找到服务地址：{}",serviceAddress);

            return new InetSocketAddress(serviceAddress.split(":")[0],Integer.parseInt(serviceAddress.split(":")[1]));

    }
}
