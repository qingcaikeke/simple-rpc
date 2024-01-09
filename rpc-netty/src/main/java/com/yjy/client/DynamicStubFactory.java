package com.yjy.client;

import com.itranswarp.compiler.JavaStringCompiler;
import com.yjy.transport.Transport;

import java.util.Map;
/**
 * DynamicStubFactory类通过反射获取传入的服务类对象的信息，
 * 然后使用这些信息填充了一个存根（stub）的源代码模板。
 * 接着，它使用动态编译器（JavaStringCompiler）将填充好的源代码编译成字节码，并通过类加载器加载生成的类。
 * 生成的类就是一个动态代理类，也就是所说的桩（stub）。
 * 这个桩类具有与服务类相同的方法签名，但它的实现中包含了远程调用的逻辑。
 * 当你调用这个桩的方法时，实际上是通过网络传输调用了远程服务的相应方法。
 */

/**
 * 限定：服务接口只能有一个方法，并且这个方法只能有一个参数，参数和返回值的类型都是 String 类型。
 */
public class DynamicStubFactory implements StubFactory{
    private final static String STUB_SOURCE_TEMPLATE =
            "package com.yjy.client.stubs;\n" +
                    "import com.github.liyue2008.rpc.serialize.SerializeSupport;\n" +
                    "\n" +
                    "public class %s extends AbstractStub implements %s {\n" +
                    "    @Override\n" +
                    "    public String %s(String arg) {\n" +
                    "        return SerializeSupport.parse(\n" +
                    "                invokeRemote(\n" +
                    "                        new RpcRequest(\n" +
                    "                                \"%s\",\n" +
                    "                                \"%s\",\n" +
                    "                                SerializeSupport.serialize(arg)\n" +
                    "                        )\n" +
                    "                )\n" +
                    "        );\n" +
                    "    }\n" +
                    "}";
    @Override
    @SuppressWarnings("unchecked")
    public <T> T createStub(Transport transport, Class<T> serviceClass) {
        try {
            // 填充模板
            String stubSimpleName = serviceClass.getSimpleName() + "Stub";
            String classFullName = serviceClass.getName();
            String stubFullName = "com.yjy.client.stubs." + stubSimpleName;
            String methodName = serviceClass.getMethods()[0].getName();

            String source = String.format(STUB_SOURCE_TEMPLATE, stubSimpleName, classFullName, methodName, classFullName, methodName);
            // 编译源代码
            JavaStringCompiler compiler = new JavaStringCompiler();
            Map<String, byte[]> results = compiler.compile(stubSimpleName + ".java", source);
            // 加载编译好的类
            Class<?> clazz = compiler.loadClass(stubFullName, results);

            // 把Transport赋值给桩
            ServiceStub stubInstance = (ServiceStub) clazz.newInstance();
            stubInstance.setTransport(transport);
            // 返回这个桩
            return (T) stubInstance;
        }catch (Throwable t){
            throw new RuntimeException(t);
        }

    }
}
