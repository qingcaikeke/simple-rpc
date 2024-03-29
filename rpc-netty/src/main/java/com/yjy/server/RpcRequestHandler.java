package com.yjy.server;

import com.yjy.client.ServiceTypes;
import com.yjy.client.stubs.RpcRequest;
import com.yjy.serialize.SerializeSupport;
import com.yjy.spi.Singleton;
import com.yjy.transport.RequestHandler;
import com.yjy.transport.command.Code;
import com.yjy.transport.command.Command;
import com.yjy.transport.command.Header;
import com.yjy.transport.command.ResponseHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 本例中服务端只需要处理一种类型的请求，所以只实现了一个命令处理器
 * 具体的请求处理器实现了请求处理和服务注册（请求名，请求实例）
 */

@Singleton//单例
public class RpcRequestHandler implements RequestHandler, ServiceProviderRegistry{
    private static final Logger logger = LoggerFactory.getLogger(RpcRequestHandler.class);
    private Map<String/*服务名*/, Object/*服务实现类的实例*/> serviceProviders = new HashMap<>();

    /**
     * 接到 requestCommand 请求命令
     * 里面有请求头和request，request里有方法名，方法参数
     * 根据方法名找到对应的provider，反射得到对应的方法，调用得到结果
     * 封装结果返回
     */
    @Override
    public Command handle(Command requestCommand) {
        Header header = requestCommand.getHeader();
        // 从payload中反序列化RpcRequest
        RpcRequest rpcRequest = SerializeSupport.parse(requestCommand.getPayload());
        try {
            // 查找所有已注册的服务提供方，寻找rpcRequest中需要的服务
            Object serviceProvider = serviceProviders.get(rpcRequest.getInterfaceName());
            if(serviceProvider != null) {
                // 找到服务提供者，利用Java反射机制调用服务的对应方法
                String arg = SerializeSupport.parse(rpcRequest.getSerializedArguments());
                Method method = serviceProvider.getClass().getMethod(rpcRequest.getMethodName(), String.class);
                String result = (String ) method.invoke(serviceProvider, arg);
                // 把结果封装成响应命令并返回
                return new Command(new ResponseHeader(type(), header.getVersion(), header.getRequestId()), SerializeSupport.serialize(result));
            }
            // 如果没找到，返回NO_PROVIDER错误响应。
            logger.warn("No service Provider of {}#{}(String)!", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
            return new Command(new ResponseHeader(type(), header.getVersion(), header.getRequestId(), Code.NO_PROVIDER.getCode(), "No provider!"), new byte[0]);
        } catch (Throwable t) {
            // 发生异常，返回UNKNOWN_ERROR错误响应。
            logger.warn("Exception: ", t);
            return new Command(new ResponseHeader(type(), header.getVersion(), header.getRequestId(), Code.UNKNOWN_ERROR.getCode(), t.getMessage()), new byte[0]);
        }
    }

    /**
     *支持的请求类型，本例中只支持rpc请求
     */
    @Override
    public int type() {
        return ServiceTypes.TYPE_RPC_REQUEST;
    }

    @Override
    public <T> void addServiceProvider(Class<? extends T> serviceClass, T serviceProvider) {
        serviceProviders.put(serviceClass.getCanonicalName(), serviceProvider);
        logger.info("Add service: {}, provider: {}.",
                serviceClass.getCanonicalName(),
                serviceProvider.getClass().getCanonicalName());
    }
}
