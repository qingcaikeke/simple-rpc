package com.yjy.transport.netty;

import com.yjy.transport.InFlightRequests;
import com.yjy.transport.ResponseFuture;
import com.yjy.transport.Transport;
import com.yjy.transport.command.Command;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.Channel;
import java.util.concurrent.CompletableFuture;

/**
 * 异步方法，发送请求直接就回来
 */
public class NettyTransport implements Transport {
    //代表了一个可以进行 I/O 操作的开放连接，定义了基本的 I/O 操作，如读、写、连接、关闭等。
    private final Channel channel;
    //存放了所有在途的请求，也就是已经发出了请求但还没有收到响应的这些 responseFuture 对象
    private final InFlightRequests inFlightRequests;
    NettyTransport(Channel channel, InFlightRequests inFlightRequests) {
        this.channel = channel;
        this.inFlightRequests = inFlightRequests;
    }
    @Override
    public CompletableFuture<Command> send(Command request) {
        // 构建返回值
        CompletableFuture<Command> completableFuture = new CompletableFuture<>();
        try {
            // 构建了一个 ResponseFuture 对象，放到 inFlightRequests 中
            inFlightRequests.put(new ResponseFuture(request.getHeader().getRequestId(), completableFuture));
            // 2.发送命令
            //调用 netty 发送数据的方法，把这个 request 命令发给对方
            channel.writeAndFlush(request).addListener((ChannelFutureListener) channelFuture -> {
                // 处理发送失败的情况
                if (!channelFuture.isSuccess()) {
                    completableFuture.completeExceptionally(channelFuture.cause());
                    channel.close();
                }
            });
        } catch (Throwable t) {
            // 处理发送异常
            inFlightRequests.remove(request.getHeader().getRequestId());
            completableFuture.completeExceptionally(t);
        }
        return completableFuture;

    }
}
