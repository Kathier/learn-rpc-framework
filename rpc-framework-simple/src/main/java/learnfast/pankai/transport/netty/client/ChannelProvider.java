package learnfast.pankai.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import learnfast.pankai.enumration.RpcErrorMessageEnum;
import learnfast.pankai.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by PanKai on 2021/2/22 17:07
 * 用于获取channel对象
 * @Description
 **/
public class ChannelProvider {
    private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);
    private  static Bootstrap bootstrap=NettyClient.initializeBootstrap();
    private static Channel channel;
    //最多重试次数
    private  static  final  int MAX_RETRY_TIME=5;

    public  static  Channel get(InetSocketAddress inetSocketAddress){
        /**
         * 实现多个线程开始执行任务的最大并行性。注意是并行性，不是并发，强调的是多个线程在某一时刻同时开始执行。
         * 类似于赛跑，将多个线程放到起点，等待发令枪响，然后同时开跑。
         * 初始化一个共享的CountDownLatch(1)，将其计数器初始化为1，
         * 多个线程在开始执行任务前首先 coundownlatch.await()，当主线程调用 countDown() 时，计数器变为0，多个线程同时被唤醒。
         */
        try {
            CountDownLatch countDownLatch=new CountDownLatch(1);
            connect(bootstrap,inetSocketAddress,countDownLatch);
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("occur exception when get channel ",e);
        }
        return  channel;
    }
    private  static  void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress,  CountDownLatch countDownLatch){
        connect(bootstrap,inetSocketAddress,MAX_RETRY_TIME,countDownLatch);
    }
    /**
     *
     * @param bootstrap
     * @param inetSocketAddress 此类用于实现 IP 套接字地址 (IP 地址+端口号)，用于socket 通信
     * @param retry
     * @param countDownLatch  是一个同步类工具，不涉及锁定，当count的值为零时当前线程继续运行，不涉及同步，
     *                       只涉及线程通信的时候，使用它较为合适
     */
    private  static  void connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress, int retry, CountDownLatch countDownLatch){
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future ->{
            if(future.isSuccess()){
                logger.info("客户端连接成功");
                channel=future.channel();
                //每当一个任务线程执行完毕，就将计数器减1
                // 当计数器的值变为0时，在CountDownLatch上 await() 的线程就会被唤醒。
                countDownLatch.countDown();
                return;
            }
            if(retry==0){
                logger.info("客户端连接失败，重试次数为0：放弃连接");
                countDownLatch.countDown();
                throw new RpcException(RpcErrorMessageEnum.CLIENT_CONNECT_SERVER_FAILURE);
            }
            //第几次重连
            int order=MAX_RETRY_TIME-retry+1;
            //重连的间隔
            int delay=1 << order;
            logger.info("{} 连接失败，第 {} 次重连",new Date(),order);
            //定时任务是调用 bootstrap.config().group().schedule(),
            // 其中 bootstrap.config() 这个方法返回的是 BootstrapConfig，它是对 Bootstrap 配置参数的抽象，
            // bootstrap.config().group() 返回的就是我们在一开始的时候配置的线程模型 workerGroup，
            // 调用workerGroup 的 schedule 方法即可实现定时任务逻辑。
            bootstrap.config().group().schedule(()->connect(bootstrap, inetSocketAddress, retry-1, countDownLatch),delay, TimeUnit.SECONDS);

        });
    }
}
