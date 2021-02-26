package learnfast.pankai;

import learnfast.pankai.provider.ServiceProviderImpl;

import learnfast.pankai.transport.netty.server.NettyServer;

/**
 * Created by PanKai on 2021/2/20 21:49
 *
 * @Description
 **/
public class NettyServerMain {
    public  static void main(String [] args){
        HelloService helloService=new HelloServiceImpl();
        NettyServer nettyServer =new NettyServer("127.0.0.1",9999);
        nettyServer.publishService(helloService,HelloService.class);
    }


}
