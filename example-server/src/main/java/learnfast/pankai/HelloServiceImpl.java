package learnfast.pankai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by PanKai on 2021/2/18 16:53
 * @Description
 **/
public class HelloServiceImpl implements HelloService {
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);
    @Override
    public String hello(Hello hello){
        logger.info("helloServiceImpl收到：{}.",hello.getMessage());
        String result="Hello description is " + hello.getDescription();
        logger.info("helloServiceImpl 返回：{}.",result);
        return result;
    };

}
