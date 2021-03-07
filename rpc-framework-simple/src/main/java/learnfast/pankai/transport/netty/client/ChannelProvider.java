package learnfast.pankai.transport.netty.client;
import io.netty.channel.Channel;
import learnfast.pankai.factory.SingletonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Created by PanKai on 2021/2/22 17:07
 * 用于获取channel对象
 * @Description
 **/
public class ChannelProvider {
    private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);
    private static Map<String, Channel> channels = new ConcurrentHashMap<>();
    private static NettyClient nettyClient;

    static {
        nettyClient = SingletonFactory.getInstance(NettyClient.class);
    }

    private ChannelProvider() {

    }

    public  static  Channel get(InetSocketAddress inetSocketAddress){
        String key = inetSocketAddress.toString();
        // 判断是否有对应地址的链接
        if (channels.containsKey(key)) {
            Channel channel = channels.get(key);
            // 如果有的话，判断连接是否可用，可用的话就直接获取
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channels.remove(key);
            }
        }
       return  null;
    }
    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        channels.put(key, channel);
    }

    public void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        channels.remove(key);
        logger.info("Channel map size :[{}]", channels.size());
    }
}
