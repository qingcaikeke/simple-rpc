package com.yjy.nameservice;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
/**
 * 注册中心的元数据，客户端查询服务地址，会从元数据中拿服务地址（uri）返回给客户端
 * 一个map：服务名+服务提供者的uri列表，用于注册中心中服务注册和查找
 * uri用于RpcAccessPoint中找到transport进而构建桩
 */
public class Metadata extends HashMap<String /*服务名*/, List<URI>/*服务提供者URI列表*/> {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Metadata:").append("\n");
        for (Entry<String, List<URI>> entry : entrySet()) {
            sb.append("\t").append("Classname: ")
                    .append(entry.getKey()).append("\n");
            sb.append("\t").append("URIs:").append("\n");
            for (URI uri : entry.getValue()) {
                sb.append("\t\t").append(uri).append("\n");
            }
        }
        return sb.toString();
    }
}
