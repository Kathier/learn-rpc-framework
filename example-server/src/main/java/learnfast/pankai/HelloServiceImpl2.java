package learnfast.pankai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by PanKai on 2021/2/18 21:23
 *
 * @Description
 **/
public class HelloServiceImpl2 implements HelloService,HiService{

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    public String hello(Hello hello) {
        logger.info("HelloServiceImpl2收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        logger.info("HelloServiceImpl2返回: {}.", result);
        return result;
    }

    @Override
    public String sayhi(Hello hello) {
        logger.info("HelloServiceImpl2收到: {}.", hello.getMessage());
        String result = "Hi description is " + hello.getDescription();
        logger.info("HelloServiceImpl2返回: {}.", result);
        return result;
    }
}
