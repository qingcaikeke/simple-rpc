package com.yjy.server;

/**
 * 与RpcAccessPoint中的addServiceProvider对应
 * 实现服务端端注册自己的服务到注册中心（此处为registry）
 * 注册内容包括接口类clazz和服务实现实例
 */
public interface ServiceProviderRegistry {
    <T> void addServiceProvider(Class<? extends T> serviceClass, T serviceProvider);
}
