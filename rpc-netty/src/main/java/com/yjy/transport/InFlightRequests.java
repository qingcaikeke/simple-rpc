package com.yjy.transport;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 存放了所有在途的请求，也就是已经发出了请求但还没有收到响应的这些 responseFuture 对象
 */
public class InFlightRequests implements Closeable {
    //timeout:超时，请求的超时时间
    private final static long TIMEOUT_SEC = 10L;
    //信号量，用于限制同时进行的请求数量
    //这个信号量有 10 个许可，我们每次往 inFlightRequest 中加入一个 ResponseFuture 的时候，
    // 需要先从信号量中获得一个许可，如果这时候没有许可了，就会阻塞当前这个线程，
    // 也就是发送请求的这个线程，直到有人归还了许可，才能继续发送请求。
    private final Semaphore semaphore = new Semaphore(10);
    //存储请求ID, 请求的 ResponseFuture 对象
    private final Map<Integer, ResponseFuture> futureMap = new ConcurrentHashMap<>();
    //定时任务的单线程执行器
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    //定时清理超时请求的 ScheduledFuture 对象
    private final ScheduledFuture scheduledFuture;

    /**
     * 构造方法
     * 初始化了 scheduledFuture，
     * 以固定的时间间隔定期执行 removeTimeoutFutures 方法，清理超时的请求
     */
    public InFlightRequests() {
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(this::removeTimeoutFutures, TIMEOUT_SEC, TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    /**
     * 将新的请求添加到 futureMap 中。
     * 它首先尝试通过 semaphore 获取许可，
     * 如果成功获取则将请求添加到映射中，
     * 否则抛出 TimeoutException 表示请求超时。
     */
    public void put(ResponseFuture responseFuture) throws InterruptedException, TimeoutException {
        //服务端处理不过来的时候，客户端还一直不停地发请求显然是没有意义的
        //限制一下客户端的请求速度
        if(semaphore.tryAcquire(TIMEOUT_SEC, TimeUnit.SECONDS)) {
            futureMap.put(responseFuture.getRequestId(), responseFuture);
        } else {
            throw new TimeoutException();
        }
    }

    /**
     * 清理超时请求
     * 超时则从映射中移除，并释放一个信号量
     */
    private void removeTimeoutFutures() {
        futureMap.entrySet().removeIf(entry -> {
            if( System.nanoTime() - entry.getValue().getTimestamp() > TIMEOUT_SEC * 1000000000L) {
                semaphore.release();
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     *映射中移除指定请求ID对应的请求，并释放一个信号量许可
     * transport发送异常异常时会调用
     */
    public ResponseFuture remove(int requestId) {
        ResponseFuture future = futureMap.remove(requestId);
        if(null != future) {
            semaphore.release();
        }
        return future;
    }


    @Override
    public void close() {
        scheduledFuture.cancel(true);
        scheduledExecutorService.shutdown();
    }
}
