package com.yjy.transport.command;

import java.util.HashMap;
import java.util.Map;

public enum Code {
    /**
     * 枚举常量，可以认为是对象
     */
    SUCCESS(0, "SUCCESS"),
    NO_PROVIDER(-2, "NO_PROVIDER"),
    UNKNOWN_ERROR(-1, "UNKNOWN_ERROR");
    /**
     * 静态 Map 用于检索枚举常量
     * 在静态块中，遍历所有枚举常量，并将其加入到这个映射中。
     */
    private static Map<Integer,Code> codes = new HashMap<>();

    private int code;
    private String message;

    /**
     * 在静态块中，遍历所有枚举常量，并将其加入到这个映射中。
     */
    static{
        for(Code code:Code.values()){
            codes.put(code.code,code);
        }
    }
    Code(int code, String message) {
        this.code = code;
        this.message = message;
    }
    /**
     * 通过传入的整数码返回相应的枚举常量。它利用了前面构建的 codes 映射。
     */
    public static Code valueOf(int code) {
        return codes.get(code);
    }

    /**
     *Getter 方法
     * 用于获取枚举常量的整数码和消息
     * getMessage 方法支持使用可变参数来替换消息字符串中的占位符
     * 这种方式使得你可以使用 Code.SUCCESS, Code.NO_PROVIDER 等枚举常量来表示不同的状态
     * 并通过调用相应的方法获取状态码和消息
     * 实现在处理错误码和消息时更加清晰和易于维护
     */
    public int getCode() {
        return code;
    }
    public String getMessage(Object... args) {
        if (args.length < 1) {
            return message;
        }
        return String.format(message, args);
    }

}
