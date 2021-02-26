package learnfast.pankai;

import learnfast.pankai.transport.ClientTransport;
import learnfast.pankai.proxy.RpcClientProxy;
import learnfast.pankai.transport.netty.client.NettyClientTransport;

import java.net.InetSocketAddress;

/**
 * Created by PanKai on 2021/2/20 22:01
 *
 * @Description
 **/
public class NettyClientMain {
    public  static  void main(String [] args){
       ClientTransport clientTransport =  new NettyClientTransport();
       RpcClientProxy rpcClientProxy=new RpcClientProxy(clientTransport);
       HelloService helloService= rpcClientProxy.getProxy(HelloService.class);
       String hello=helloService.hello(new Hello("test","何人为我楚舞"));
       System.out.println(hello);

    }
}
