package com.yjy.client;
import com.yjy.transport.Transport;

/**
 * 客户端提供代理对象，也就是“桩”
 * 委托对象在服务端，实现具体逻辑
 */
public interface StubFactory {
    /**
     *
     * @param transport:有一个给服务端发送请求的方法
     * @param serviceClass：告诉桩工厂，要创建一个什么类型的桩
     * @return：返回一个工厂创建出来的桩
     * @param <T>
     */
    <T> T createStub(Transport transport, Class<T> serviceClass);
}
