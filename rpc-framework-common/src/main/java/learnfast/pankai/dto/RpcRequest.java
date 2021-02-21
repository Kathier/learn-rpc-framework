package learnfast.pankai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author PanKai
 * 对象在进行网络传输（比如远程方法调用Rpc时)之前需要先被序列化，接收到序列化的对象之后需要进行反序列化
 * JDK自带的序列化方式
 * 缺点：1，不支持跨语言调用
 *      2，性能差：序列化之后的字节数组体积较大，导致传输成本增大
 * 服务端需要哪些信息，才能唯一确定服务端需要调用的接口的方法呢？
 * 首先，就是接口的名字，和方法的名字，但是由于方法重载的缘故，我们还需要这个方法的所有参数的类型，
 * 最后，客户端调用时，还需要传递参数的实际值，那么服务端知道以上四个条件，就可以找到这个方法并且调用了。
 * 我们把这四个条件写到一个对象里，到时候传输时传输这个对象就行了。即RpcRequest对象：
 * @Description //RpcRequest实体类包含接口名，方法名，参数数组及参数类型数组
 * @Date 2020/2/15
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {
    private   String interfaceName;
    private   String methodName;
    private Object [] parameters;
    private Class <?> [] parameterTypes;


}
