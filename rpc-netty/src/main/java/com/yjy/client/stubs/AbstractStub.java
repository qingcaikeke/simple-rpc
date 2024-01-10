package com.yjy.client.stubs;

import com.yjy.client.RequestIdSupport;
import com.yjy.client.ServiceStub;
import com.yjy.client.ServiceTypes;
import com.yjy.serialize.SerializeSupport;
import com.yjy.transport.Transport;
import com.yjy.transport.command.Code;
import com.yjy.transport.command.Command;
import com.yjy.transport.command.Header;
import com.yjy.transport.command.ResponseHeader;

import java.util.concurrent.ExecutionException;

/**
 * 抽象桩实现大部分通用的逻辑，让所有动态生成的桩都继承这个抽象类
 * 构造header，封装request（方法名，方法参数），NettyTransport发送请求，异步从future中得到响应bytes[]
 */
public abstract class AbstractStub implements ServiceStub {
    protected Transport transport;

    protected byte [] invokeRemote(RpcRequest request) {
        Header header = new Header(ServiceTypes.TYPE_RPC_REQUEST, 1, RequestIdSupport.next());
        byte [] payload = SerializeSupport.serialize(request);
        Command requestCommand = new Command(header, payload);
        try {
            //发送请求，得到CompletableFuture，从future中的到response
            Command responseCommand = transport.send(requestCommand).get();
            ResponseHeader responseHeader = (ResponseHeader) responseCommand.getHeader();
            if(responseHeader.getCode() == Code.SUCCESS.getCode()) {
                return responseCommand.getPayload();
            } else {
                throw new Exception(responseHeader.getError());
            }

        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setTransport(Transport transport) {
        this.transport = transport;
    }

}
