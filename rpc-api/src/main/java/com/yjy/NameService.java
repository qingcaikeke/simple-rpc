package com.yjy;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

/**
 * 注册中心
 * 1.给调用方提供api
 * 2.并实现与服务端的通信
 * 3.记录rpc服务发来的注册信息，保存到元数据中
 * 4.客户端查寻服务地址的时候，从元数据中获取服务地址，返回给客户端
 */
public interface NameService {

    /**
     * 所有支持的协议
     */
    Collection<String> supportedSchemes();

    /**
     * 连接注册中心
     * @param nameServiceUri 注册中心地址
     */
    void connect(URI nameServiceUri);
    /**
     * 服务端注册服务
     * @param serviceName 服务名称
     * @param uri 服务地址
     */
    void registerService(String serviceName, URI uri) throws IOException;

    /**
     * 客户端查询服务地址
     * @param serviceName 服务名称
     * @return 服务地址
     */
    URI lookupService(String serviceName) throws IOException;
}

