package com.yjy.transport;

import com.yjy.transport.command.Command;

import java.util.concurrent.CompletableFuture;

/**
 * 发送请求，实现异步通信
 * 接收command，包含请求头和消息数组
 */
public interface Transport {
    /**
     * 发送请求命令
     * @param request 请求命令
     * @return 返回值是一个Future，Future
     * 使用一个 CompletableFuture 作为返回值，使用起来就非常灵活，
     * 我们可以直接调用它的 get 方法来获取响应数据，这就相当于同步调用；
     * 也可以使用以 then 开头的一系列异步方法，指定当响应返回的时候，需要执行的操作，就等同于异步调用。
     *  于，这样一个方法既可以同步调用，也可以异步调用。
     */

    CompletableFuture<Command> send(Command request);
}
