package com.yjy.transport.command;

public class Header {
    //唯一标识一个请求命令,用于请求和响应的配对
    private int requestId;
    //版本号 例如http1.1,2.0等，
    private int version;
    //命令的类型,让接收方来识别收到的是什么命令，以便路由到对应的处理类
    private int type;
    public Header() {}
    public Header(int type, int version, int requestId) {
        this.requestId = requestId;
        this.type = type;
        this.version = version;
    }
    public int getRequestId() {
        return requestId;
    }
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }

    /**
     *
     * @return
     */
    public int length() {
        return Integer.BYTES + Integer.BYTES + Integer.BYTES;
    }
}
