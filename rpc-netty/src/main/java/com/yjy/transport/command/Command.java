package com.yjy.transport.command;

/**
 * 命令包括 1.请求头header 2.序列化后的有效载荷byte[]
 */
public class Command {
    protected Header header;
    //要传输的数据,也就是被序列化之后生成的字节数组,payload(有效载荷)
    private byte [] payload;
    public Command(Header header, byte [] payload) {
        this.header = header;
        this.payload = payload;
    }
    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public byte [] getPayload() {
        return payload;
    }

    public void setPayload(byte [] payload) {
        this.payload = payload;
    }
}
