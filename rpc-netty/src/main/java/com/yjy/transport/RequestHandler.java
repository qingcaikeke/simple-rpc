package com.yjy.transport;

import com.yjy.transport.command.Command;

/**
 * 请求处理器
 * 1.注册到handlerRegistry type-handler
 * 2.handle：获得serviceProvider完成处理，注册serviceProvider（名字-实例）
 */
public interface RequestHandler {
    /**
     * 处理请求
     * @param requestCommand 请求命令
     * @return 响应命令
     */
    Command handle(Command requestCommand);

    /**
     * 支持的请求类型
     */
    int type();
}
