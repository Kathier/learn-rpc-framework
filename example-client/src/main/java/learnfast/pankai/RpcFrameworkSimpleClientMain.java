package learnfast.pankai;

import learnfast.pankai.transport.ClientTransport;
import learnfast.pankai.proxy.RpcClientProxy;
import learnfast.pankai.transport.socket.SocketRpcClient;

/**
 * Created by PanKai on 2021/2/18 17:17
 *
 * @Description
 **/
public class RpcFrameworkSimpleClientMain {
    public static void main(String [] args){
        ClientTransport clientTransport =new SocketRpcClient();
        RpcClientProxy rpcClientProxy=new RpcClientProxy(clientTransport);
        HelloService helloService=rpcClientProxy.getProxy(HelloService.class);
        String hello=helloService.hello(new Hello("test","何人为我楚舞"));
        System.out.println(hello);

        System.out.println("注册多个服务");
        HiService hiService=rpcClientProxy.getProxy(HiService.class);
        String hi=hiService.sayhi(new Hello("test1","听我楚狂声"));
        System.out.println(hi);


    }

}
