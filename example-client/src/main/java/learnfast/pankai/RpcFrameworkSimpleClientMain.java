package learnfast.pankai;

import learnfast.pankai.remoting.socket.RpcClientProxy;

/**
 * Created by PanKai on 2021/2/18 17:17
 *
 * @Description
 **/
public class RpcFrameworkSimpleClientMain {
    public static void main(String [] args){
        RpcClientProxy rpcClientProxy=new RpcClientProxy("127.0.0.1",9999);
        HelloService helloService=rpcClientProxy.getProxy(HelloService.class);
        String hello=helloService.hello(new Hello("test","何人为我楚舞"));
        System.out.println(hello);

        System.out.println("注册多个服务");
        HiService hiService=rpcClientProxy.getProxy(HiService.class);
        String hi=hiService.sayhi(new Hello("test1","听我楚狂声"));
        System.out.println(hi);


    }

}
