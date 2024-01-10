package com.yjy.transport;

import com.yjy.transport.command.Command;

/**
 * 请求处理器
 * 一个与请求类型对应的type，一个处理函数，接收requestCommand，返回responseCommand
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
