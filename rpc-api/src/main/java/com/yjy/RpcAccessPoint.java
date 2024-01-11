package com.yjy;

import com.yjy.spi.ServiceSupport;

import java.io.Closeable;
import java.net.URI;
import java.util.Collection;

/**
 * RPC框架对外提供的服务接口
 */
public interface RpcAccessPoint extends Closeable {
    /**
     * 相当于dubbo的@Service
     * 客户端获取远程服务的引用
     * @param uri 远程服务地址
     * @param serviceClass 服务的接口类的Class
     * @param <T> 服务接口的类型
     * @return 远程服务引用
     */
    <T> T getRemoteService(URI uri, Class<T> serviceClass);

    /**
     * 相当于dubbo的@reference
     * 服务端注册服务的实现实例
     * @param service 实现实例
     * @param serviceClass 服务的接口类的Class
     * @param <T> 服务接口的类型
     * @return 服务地址
     */
    <T> URI addServiceProvider(T service, Class<T> serviceClass);

    /**
     * 获取注册中心的引用
     * 默认方法是在接口中提供一个默认的实现，这样在实现该接口的类中，如果没有为该方法提供具体实现，就会使用默认的方法。
     * 加载所有nameService的实现类，根据给定的uri中的协议，去匹配支持这个协议的实现类
     * 连接并返回这个实现的引用
     * 系统可以根据 URI 中的协议，动态地来选择不同的注册中心实现
     */

    default NameService getNameService(URI nameServiceUri) {
        Collection<NameService> nameServices = ServiceSupport.loadAll(NameService.class);
        for (NameService nameService : nameServices) {
            if(nameService.supportedSchemes().contains(nameServiceUri.getScheme())) {

                nameService.connect(nameServiceUri);
                return nameService;
            }
        }
        return null;
    }

    /**
     * 服务端启动RPC框架，监听接口，开始提供远程服务。
     * @return 服务实例，用于程序停止的时候安全关闭服务。
     */
    Closeable startServer() throws Exception;
}

