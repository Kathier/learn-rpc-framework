package learnfast.pankai;

import learnfast.pankai.transport.RpcClient;
import learnfast.pankai.transport.RpcClientProxy;
import learnfast.pankai.transport.netty.NettyRpcClient;

/**
 * Created by PanKai on 2021/2/20 22:01
 *
 * @Description
 **/
public class NettyClientMain {
    public  static  void main(String [] args){
       RpcClient rpcClient=  new NettyRpcClient("127.0.0.1",9999);
       RpcClientProxy rpcClientProxy=new RpcClientProxy(rpcClient);
       HelloService helloService= rpcClientProxy.getProxy(HelloService.class);
       String hello=helloService.hello(new Hello("test","何人为我楚舞"));
       System.out.println(hello);

    }
}
