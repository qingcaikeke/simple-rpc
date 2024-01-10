package com.yjy.transport.netty;

import com.yjy.transport.InFlightRequests;
import com.yjy.transport.ResponseFuture;
import com.yjy.transport.command.Command;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * 异步接收所有服务端返回的响应
 * 根据响应头中的 requestId，去在途请求 inFlightRequest 中查找对应的 ResponseFuture
 * 拿到里面的CompletableFuture，设置返回值并结束这个 ResponseFuture 就可以了
 */
@ChannelHandler.Sharable
//Invocation:调用，祈求
public class ResponseInvocation extends SimpleChannelInboundHandler<Command> {
    private static final Logger logger = LoggerFactory.getLogger(ResponseInvocation.class);
    private final InFlightRequests inFlightRequests;

    ResponseInvocation(InFlightRequests inFlightRequests) {
        this.inFlightRequests = inFlightRequests;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Command response) {
        ResponseFuture future = inFlightRequests.remove(response.getHeader().getRequestId());
        if(null != future) {
            CompletableFuture<Command> completableFuture = future.getFuture();
            completableFuture.complete(response);
        } else {
            logger.warn("Drop response: {}", response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Exception: ", cause);
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if(channel.isActive())ctx.close();
    }
}
