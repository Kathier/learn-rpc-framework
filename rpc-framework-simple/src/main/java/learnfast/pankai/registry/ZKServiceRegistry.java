package learnfast.pankai.registry;

import learnfast.pankai.util.CuratorHelper;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by PanKai on 2021/2/22 21:23
 * 基于zookeeper实现服务注册中心
 **/
public class ZKServiceRegistry implements ServiceRegistry{
    private static final Logger logger = LoggerFactory.getLogger(ZKServiceRegistry.class);
    private  final CuratorFramework zkClient;
    public  ZKServiceRegistry(){
        zkClient=CuratorHelper.getZKClient();
        zkClient.start();
    }
    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {
        //根节点下注册子节点服务
        StringBuilder servicePath=new StringBuilder(CuratorHelper.ZK_REGISTER_ROOT_PATH).append("/").append(serviceName);
        //服务子节点下注册子节点：服务地址
        servicePath.append(inetSocketAddress.toString());
        CuratorHelper.createEphemeralNode(zkClient,servicePath.toString());
        logger.info("节点创建成功，节点为{} ",servicePath);


    }

    @Override
    public InetSocketAddress lookUpService(String serviceName) {
        String serviceAddress=CuratorHelper.getChildrenNodes(zkClient,serviceName).get(0);
        logger.info("成功找到服务地址：{}",serviceAddress);

        return new InetSocketAddress(serviceAddress.split(":")[0],Integer.parseInt(serviceAddress.split(":")[1]));
    }
}
