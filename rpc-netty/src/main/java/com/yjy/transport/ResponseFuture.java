package com.yjy.transport;

import com.yjy.transport.command.Command;

import java.util.concurrent.CompletableFuture;

/**
 * 用于异步获取远程调用结果的机制
 *代表一个尚未完成的远程调用，允许客户端在未来的某个时间点获取到调用的结果。
 * 包含requestId，CompletableFuture，和时间戳
 */
public class ResponseFuture {
    private final int requestId;
    private final CompletableFuture<Command> future;
    private final long timestamp;
    public ResponseFuture(int requestId, CompletableFuture<Command> future) {
        this.requestId = requestId;
        this.future = future;
        timestamp = System.nanoTime();
    }

    public int getRequestId() {
        return requestId;
    }
    public CompletableFuture<Command> getFuture() {
        return future;
    }

    long getTimestamp() {
        return timestamp;
    }

}
