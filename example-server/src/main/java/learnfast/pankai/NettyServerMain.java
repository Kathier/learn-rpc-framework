package learnfast.pankai;

import learnfast.pankai.registry.DefaultServiceRegistry;

import learnfast.pankai.transport.netty.server.NettyServer;

/**
 * Created by PanKai on 2021/2/20 21:49
 *
 * @Description
 **/
public class NettyServerMain {
    public  static void main(String [] args){
        HelloService helloService=new HelloServiceImpl();
        DefaultServiceRegistry serviceRegistry=new DefaultServiceRegistry();
        //手动注册
        serviceRegistry.register(helloService);
        NettyServer nettyServer =new NettyServer(9999);
        nettyServer.run();
    }


}
