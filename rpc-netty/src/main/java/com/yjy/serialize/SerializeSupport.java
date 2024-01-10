package com.yjy.serialize;

import java.util.HashMap;
import java.util.Map;

import com.yjy.spi.ServiceLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.yjy.spi.ServiceSupport;
/**
 * 对外提供服务的就只有一个 SerializeSupport 静态类
 * 通用静态类，支持任何对象类型的序列化
 */
public class SerializeSupport {
    //clazz的作用是让logger与实例关联
    private static final Logger logger = LoggerFactory.getLogger(SerializeSupport.class);
    //序列化的时候使用，通过待序列化类型（如string）找到对应的序列化器serializer，通过序列化器完成反序列化
    private static Map<Class<?>/*序列化对象类型*/, Serializer<?>/*序列化实现*/> serializerMap = new HashMap<>();

    //反序列化的时候使用，通过bytes数组中的第一个元素：type，找到对应的被序列化类型（String），进而找到序列化器serializer
    private static Map<Byte/*序列化实现类型*/, Class<?>/*序列化对象类型*/> typeMap = new HashMap<>();

    /**
     * 通过ServiceSupport.loadAll(Serializer.class)加载所有实现了Serializer接口的类，
     * 然后通过registerType方法注册这些序列化实现。这个块会在类加载时执行，用于初始化已注册的序列化器
     */
    static {
        for(Serializer serializer : ServiceSupport.loadAll(Serializer.class) ){
            registerType(serializer.type(), serializer.getSerializeClass(), serializer);

            logger.info("Found serializer, class: {}, type: {}.",
                    serializer.getSerializeClass().getCanonicalName(),//规范化类名
                    serializer.type());
        }
    }

    /**
     * 用于注册序列化实现和对应的序列化对象类型。
     * serializerMap用于根据对象类型查找序列化实现，
     * typeMap用于根据序列化实现类型查找对象类型
     * @param type
     * @param eClass
     * @param serializer
     * @param <E>
     */
    public static <E> void registerType(byte type, Class<E> eClass, Serializer<E> serializer){
        serializerMap.put(eClass,serializer);
        typeMap.put(type,eClass);
    }

    /**
     *用于从字节数组中获取被序列化的类型标号：type
     */
    private static byte parseEntryType(byte[] buffer) {
        return buffer[0];
    }

    /**
     * 从map中根据type获取实际的被序列化对象类型String
     */
    private static <E> E parse(byte[] buffer, int offset, int length) {
        byte type = parseEntryType(buffer);
        @SuppressWarnings("unchecked")
        Class<E> eClass = (Class<E>) typeMap.get(type);
        if (null == eClass) {
            throw new SerializeException(String.format("Unknown entry type: %d!", type));
        } else {
            return parse(buffer, offset + 1, length - 1, eClass);
        }
    }

    /**
     * parse的方法重载
     * 根据传来的被序列化对象类型，找到序列化器，完成序列化
     * 并进行类型检查
     */
    private static <E> E parse(byte[] buffer,int offset,int length,Class<E> eClass){
        Object entry = serializerMap.get(eClass).parse(buffer, offset, length);
        if(eClass.isAssignableFrom(entry.getClass())){
            return (E) entry;
        }else {
            throw new SerializeException("Type mismatch!");
        }
    }

    /**
     *对外公开的接口，用于从字节数组中反序列化对象。
     */
    public static <E> E parse(byte[] buffer) {
        return parse(buffer, 0, buffer.length);
    }

    /**
     *通过对象类型获取对应的序列化器，然后调用序列化实现的方法将对象序列化为字节数组。
     * 序列化字节数组的第一个字节存储了序列化实现类型
     */
    public static <E> byte[] serialize(E entry) {
        @SuppressWarnings("unchecked")
        Serializer<E> serializer = (Serializer<E>) serializerMap.get(entry.getClass());
        if (serializer == null) {
            throw new SerializeException(String.format("Unknown entry class type: %s", entry.getClass().toString()));
        }
        //根据序列化器提供的size方法构造bites数组，再通过serialize方法完成序列化存入数组
        byte[] bytes = new byte[serializer.size(entry) + 1];
        bytes[0] = serializer.type();
        serializer.serialize(entry, bytes, 1, bytes.length - 1);
        return bytes;
    }



}
