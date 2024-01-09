package com.yjy.client.stubs;

/**
 * 接口名，方法名，序列化后的参数
 */
public class RpcRequest {
    private final String interfaceName;
    private final String methodName;
    private final byte [] serializedArguments;

    public RpcRequest(String interfaceName, String methodName, byte[] serializedArguments) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.serializedArguments = serializedArguments;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public byte[] getSerializedArguments() {
        return serializedArguments;
    }
}
