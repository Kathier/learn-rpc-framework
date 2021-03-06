package learnfast.pankai.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * Created by PanKai on 2021/2/21 21:47
 * 创建线程池工具类
 * @Description
 **/
public class ThreadPoolFactory {
    /**
     * 线程池参数
     */
    private  static final  int CORE_POOL_SIZE=10;
    private  static  final  int MAXIMUM_POOL_SIZE=100;
    private  static  final  int KEEP_ALIVE_TIME=1;
    private  static  final  int BLOCKING_QUEUE_CAPACITY=100;

    private ThreadPoolFactory(){

    }

    public static ExecutorService createDefautThreadPool(String threadNamePrefix){
        return createDefautThreadPool(threadNamePrefix,false);
    }
    public static ExecutorService createDefautThreadPool(String threadNamePrefix,Boolean daemon){
        //使用有界队列
        BlockingQueue<Runnable> workQueue=new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory=createThreadFactory(threadNamePrefix,daemon);
        return new ThreadPoolExecutor(CORE_POOL_SIZE,MAXIMUM_POOL_SIZE,KEEP_ALIVE_TIME,TimeUnit.MINUTES,workQueue,threadFactory);
    }

    /**
     * 创建threadFactory 如果 threadNamePrefix不为空则使用自建threadFactory,否则使用defaultThreadFactory
     * @param threadNamePrefix 作为创建的线程名字的前缀
     * @param daemon 指定是否为daemon thread守护线程
     * @return
     */
    public  static  ThreadFactory createThreadFactory(String threadNamePrefix,Boolean daemon){
        if(threadNamePrefix!=null){
            if(!daemon){
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix+"-%d").setDaemon(daemon).build();
            }else{
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix+"-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }
}
