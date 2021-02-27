package learnfast.pankai.registry;
import org.junit.Test;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by PanKai on 2021/2/23 21:33
 **/
public class ZkServiceRegistryTest {
    @Test
    public  void should_register_service_successful_and_lookup_service_by_service_name(){
        ServiceRegistry serviceRegistry=new ZKServiceRegistry();
        InetSocketAddress givenInetSocketAddress=new InetSocketAddress("127.0.0.1",9333);
        serviceRegistry.registerService("learnfast.pankai.registry.ZkServiceRegistry",givenInetSocketAddress);
        ServiceDiscovery serviceDiscovery=new ZKServiceDiscovery();
        InetSocketAddress acquiredInetSocketAddress=serviceDiscovery.lookupService("learnfast.pankai.registry.ZkServiceRegistry");
        assertEquals(givenInetSocketAddress.toString(), acquiredInetSocketAddress.toString());
    }


}
