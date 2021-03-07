package learnfast.pankai.loadbalance;

import java.util.List;

/**
 * Created by PanKai on 2021/2/27 20:15
 **/
public abstract  class AbstractLoadBalance implements  LoadBalance{
    @Override
    public String selectServiceAddress(List<String> serviceAddresses,String rpcServiceName) {
        if(serviceAddresses==null || serviceAddresses.size()==0){
            return null;
        }
        if(serviceAddresses.size()==1){
            return  serviceAddresses.get(0);
        }
        return  doselect(serviceAddresses,rpcServiceName);
    }

    protected abstract String doselect(List<String> serviceAdresses,String rpcServiceName);
}
