Invocation：调用  invoke：调用
responseInvocation没看懂

//clazz的作用是让logger与实例关联
private static final Logger logger = LoggerFactory.getLogger(SerializeSupport.class);
entry是某方法参数，实际上是一个实例对象
entry.getClass()，getclass就是通过反射，获取对象的类的class对象
Class<?> 这样的写法，表示任意类型的Class对象

xxNameService是注册中心
xxService是服务