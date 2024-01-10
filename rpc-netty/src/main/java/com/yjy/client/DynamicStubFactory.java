package com.yjy.client;

import com.itranswarp.compiler.JavaStringCompiler;
import com.yjy.client.stubs.AbstractStub;
import com.yjy.transport.Transport;

import java.util.Map;
/**
 * 实际生成桩，接收transport和serviceClass（创建一个什么类型的桩）
 *  根据接收的信息，填充模板生成源代码，编译加载得到桩，再填充transport
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
//      示例：写死只能有一个参数，参数和返回值的类型都是 String 类型
//    public class HelloServiceStub extends AbstractStub implements HelloService{
//        @Override
//        public String hello (String arg){
//            return SerializeSupport.parse(
//                    invokeRemote(
//                        //invokeRemote会给request加上header，再封装成command，通关transport发送（在abstractStub里）
//                            new RpcRequest(HelloService,hello,SerializeSupport.serialize(arg));
//        }
//    }

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
