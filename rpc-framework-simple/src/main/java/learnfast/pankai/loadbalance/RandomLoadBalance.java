package learnfast.pankai.loadbalance;

import java.util.List;
import java.util.Random;

/**
 * Created by PanKai on 2021/2/27 20:19
 **/
public class RandomLoadBalance extends AbstractLoadBalance{
    @Override
    protected String doselect(List<String> serviceAdresses,String rpcServiceName) {
        Random random=new Random();
        return serviceAdresses.get(random.nextInt(serviceAdresses.size()));
    }


}
