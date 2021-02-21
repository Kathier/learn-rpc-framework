package learnfast.pankai.serialize;

/**
 * Created by PanKai on 2021/2/19 16:49
 *
 * @Description
 **/
public interface Serializer {
    /**
     * 序列化接口，所有序列化类都要实现这个接口
     * @param obj 待序列化的对象
     * @return  字节数组
     */
    byte [] serialize(Object obj);

    /**
     * 反序列化
     * @param bytes 序列化后字节数组
     * @param clazz 目标类
     * @param <T>
     * @return  反序列后生成的对象
     */
    <T> T deserialize(byte[] bytes,Class<T> clazz);
}
