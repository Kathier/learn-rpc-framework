package learnfast.pankai.util;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by PanKai on 2021/2/22 21:28
 **/
public class CuratorHelper {
    private static final Logger logger = LoggerFactory.getLogger(CuratorHelper.class);
    private static final int SLEEP_MS_BETWEEN_RETRIES = 100;
    private static final int MAX_RETRIES = 3;
    private static final String CONNECT_STRING = "127.0.0.1:2181";
    private static final int CONNECTION_TIMEOUT_MS = 10 * 1000;
    private static final int SESSION_TIMEOUT_MS = 60 * 1000;
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    private  static  final Map<String, List<String>> serviceRegisterMap=new ConcurrentHashMap<>();
    public  static CuratorFramework getZKClient(){
        //重试策略：重试三次，两次重试之间的间隔为100s,以防出现连接问题
        RetryPolicy retryPolicy=new RetryNTimes(MAX_RETRIES,SLEEP_MS_BETWEEN_RETRIES);
        return CuratorFrameworkFactory.builder().
                //要连接的服务器（可以是服务器列表）
                connectString(CONNECT_STRING).
                retryPolicy(retryPolicy).
                //连接超时时间10s
                connectionTimeoutMs(CONNECTION_TIMEOUT_MS).
                //会话超时时间60s
                sessionTimeoutMs(SESSION_TIMEOUT_MS).
                build();

    }
    /**
     * 创建临时节点，驻存在Zookeeper中，连接和session断开时会被删除
     */
    public  static  void createEphemeralNode(final  CuratorFramework ZKclient,final  String path){
        try {                                           //节点类型
            ZKclient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            logger.error("occur exception",e);
        }

    }

    public  static  List<String> getChildrenNodes(final  CuratorFramework zkClient,final String serviceName){
        if(serviceRegisterMap.containsKey(serviceName)){
            return serviceRegisterMap.get(serviceName);
        }
        List<String> result= Collections.emptyList();
        String servicePath=CuratorHelper.ZK_REGISTER_ROOT_PATH+"/"+serviceName;
        try {

            result=zkClient.getChildren().forPath(servicePath);
            serviceRegisterMap.put(serviceName,result);
            registerWatcher(zkClient,serviceName);
        } catch (Exception e) {
            logger.error("occur exception ",e);
        }
        return result;
    }

    /**
     * 注册监听
     * @param zkClient
     * @param serviceName 服务名称
     */
    private static void registerWatcher(CuratorFramework zkClient, String serviceName) {
        String servicePath=CuratorHelper.ZK_REGISTER_ROOT_PATH+"/"+serviceName;
        // 监听数据节点的增删改，会触发事件;第三个参数表示是否接收节点数据内容
        PathChildrenCache pathChildrenCache=new PathChildrenCache(zkClient,servicePath,true);
        PathChildrenCacheListener pathChildrenCacheListener=(CuratorFramework,pathChildrenCacheEvent)->{
            List<String> serviceAddresses=CuratorFramework.getChildren().forPath(servicePath);
            serviceRegisterMap.put(serviceName,serviceAddresses);

        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            logger.error("occur exception ",e);

        }
    }
}
