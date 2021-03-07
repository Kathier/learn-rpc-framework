package learnfast.pankai.loadbalance;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by PanKai on 2021/2/27 20:12
 **/
public interface LoadBalance {
    /**
     * 在已有的服务地址中选择一个
     * @param serviceAddresses 服务地址列表
     * @return
     */
    String selectServiceAddress(List<String> serviceAddresses,String rpcServiceName);
}
