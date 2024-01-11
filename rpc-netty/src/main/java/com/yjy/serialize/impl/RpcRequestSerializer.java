package com.yjy.serialize.impl;

import com.yjy.client.stubs.RpcRequest;
import com.yjy.serialize.Serializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class RpcRequestSerializer implements Serializer<RpcRequest> {

    @Override
    public int size(RpcRequest request) {
        //所有参数的长度加3，因为需要记录每部分的长度
        return Integer.BYTES + request.getInterfaceName().getBytes(StandardCharsets.UTF_8).length +
                Integer.BYTES + request.getMethodName().getBytes(StandardCharsets.UTF_8).length +
                Integer.BYTES + request.getSerializedArguments().length;
    }

    @Override
    public void serialize(RpcRequest request, byte[] bytes, int offset, int length) {
        //返回的内容都存到传来的空bytes中了
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);
        byte [] tmpBytes = request.getInterfaceName().getBytes(StandardCharsets.UTF_8);
        //现存了一个长度，再存的内容
        buffer.putInt(tmpBytes.length);
        buffer.put(tmpBytes);

        tmpBytes = request.getMethodName().getBytes(StandardCharsets.UTF_8);
        buffer.putInt(tmpBytes.length);
        buffer.put(tmpBytes);

        tmpBytes = request.getSerializedArguments();
        buffer.putInt(tmpBytes.length);
        buffer.put(tmpBytes);
    }

    @Override
    public RpcRequest parse(byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);
        int len = buffer.getInt();
        byte [] tmpBytes = new byte[len];
        buffer.get(tmpBytes);
        String interfaceName = new String(tmpBytes, StandardCharsets.UTF_8);

        len = buffer.getInt();
        tmpBytes = new byte[len];
        buffer.get(tmpBytes);
        String methodName = new String(tmpBytes, StandardCharsets.UTF_8);

        len = buffer.getInt();
        tmpBytes = new byte[len];
        buffer.get(tmpBytes);
        byte [] serializedArgs = tmpBytes;

        return new RpcRequest(interfaceName, methodName, serializedArgs);
    }

    @Override
    public byte type() {
        return Types.TYPE_RPC_REQUEST;
    }

    @Override
    public Class getSerializeClass() {
        return RpcRequest.class;
    }
}
