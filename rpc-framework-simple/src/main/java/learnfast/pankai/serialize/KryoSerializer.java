package learnfast.pankai.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.dto.RpcResponse;
import learnfast.pankai.exception.SerializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by PanKai on 2021/2/19 17:07
 * 使用kyro序列化框架来进行序列化
 * @Description
 **/
public class KryoSerializer implements  Serializer{
    private static final Logger logger = LoggerFactory.getLogger(KryoSerializer.class);

    /**
     * Kryo不是线程安全的，每个线程都应该有自己的Kryo,input、output实例
     * 使用ThreadLocal存储Kryo对象
     */
    private  static  final  ThreadLocal<Kryo> kryoThreadLocal=ThreadLocal.withInitial(() -> {
        Kryo kryo=new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        //默认值为true,是否关闭注册行为,关闭之后可能存在序列化问题，一般推荐设置为 true
        kryo.setReferences(true);
        //默认值为false,是否关闭循环引用，可以提高性能，但是一般不推荐设置为 true
        kryo.setRegistrationRequired(false);
        return  kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try( ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        Output output=new Output(byteArrayOutputStream)){
            Kryo kryo=kryoThreadLocal.get();
            //将对象序列化为字节数组
            kryo.writeObject(output,obj);
            //使用后要清除，因为当前线程执行完相关代码后，很可能会被重新放入线程池中，
            // 如果ThreadLocal没有被清除，该线程执行其他代码时，会把上一次的状态带进去
            kryoThreadLocal.remove();

            return output.toBytes();
        } catch (Exception e) {
           logger.info("occur exception when serialize:",e);
           throw new SerializeException("序列化失败");
        }

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(bytes);
            Input input=new Input(byteArrayInputStream);
        ){
            Kryo kryo=kryoThreadLocal.get();
            //byte->object
            Object o=kryo.readObject(input,clazz);
            kryoThreadLocal.remove();
            return  clazz.cast(o);
        } catch (Exception e) {
           logger.info("occur exception when deserialize:",e);
           throw new SerializeException("反序列化失败");

        }

    }
}
