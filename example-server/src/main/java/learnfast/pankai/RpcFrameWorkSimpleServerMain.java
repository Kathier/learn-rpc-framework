package learnfast.pankai;

import learnfast.pankai.registry.DefaultServiceRegistry;
import learnfast.pankai.transport.socket.SocketRpcServer;

/**
 * Created by PanKai on 2021/2/18 17:03
 * @Description Rpc原理
 * 客户端和服务端都可以访问到通用的接口，但是只有服务端有这个接口的实现类，
 * 客户端调用这个接口的方式，是通过网络传输，告诉服务端我要调用这个接口，服务端收到之后找到这个接口的实现类，
 * 并且执行，将执行的结果返回给客户端，作为客户端调用接口方法的返回值。
 **/
public class RpcFrameWorkSimpleServerMain {
    public  static void main(String [] args){
        HelloService helloService=new HelloServiceImpl2();
        HiService hiService=new HelloServiceImpl2();
        DefaultServiceRegistry defaultServiceRegistry=new DefaultServiceRegistry();
        //手动注册
        defaultServiceRegistry.register(helloService);
        defaultServiceRegistry.register(hiService);
        SocketRpcServer socketRpcServer =new SocketRpcServer();
        socketRpcServer.start(9999);
    }

}
