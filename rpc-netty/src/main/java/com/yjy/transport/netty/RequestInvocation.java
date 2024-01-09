package com.yjy.transport.netty;

import com.yjy.transport.RequestHandler;
import com.yjy.transport.RequestHandlerRegistry;
import com.yjy.transport.command.Command;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * invocation：调用
 * RPC 框架的服务端如何来处理客户端发送的 RPC 请求
 * RPC 框架的服务端主要需要实现下面这两个功能：
 * 1.服务端的业务代码把服务的实现类注册到 RPC 框架中 ;
 * 2.接收客户端桩发出的请求，调用服务的实现类并返回结果。
 */
@ChannelHandler.Sharable
public class RequestInvocation extends SimpleChannelInboundHandler<Command> {
    private static final Logger logger = LoggerFactory.getLogger(RequestInvocation.class);
    private final RequestHandlerRegistry requestHandlerRegistry;

    RequestInvocation(RequestHandlerRegistry requestHandlerRegistry) {
        this.requestHandlerRegistry = requestHandlerRegistry;
    }

    /**
     *从请求中拿到对应的请求类型，找到对应的请求处理器，
     * 处理后把结果发送给客户端
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Command request) throws Exception {
        RequestHandler handler = requestHandlerRegistry.get(request.getHeader().getType());
        if(null != handler) {
            Command response = handler.handle(request);
            if(null != response) {
                channelHandlerContext.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> {
                    if (!channelFuture.isSuccess()) {
                        logger.warn("Write response failed!", channelFuture.cause());
                        channelHandlerContext.channel().close();
                    }
                });
            } else {
                logger.warn("Response is null!");
            }
        } else {
            throw new Exception(String.format("No handler for request with type: %d!", request.getHeader().getType()));
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
