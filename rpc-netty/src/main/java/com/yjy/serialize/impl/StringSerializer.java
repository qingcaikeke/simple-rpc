package com.yjy.serialize.impl;

import com.yjy.serialize.Serializer;

import java.nio.charset.StandardCharsets;
//一个支持 String 类型的序列化器
public class StringSerializer implements Serializer<String> {

    @Override
    public int size(String entry) {
        //一定要指定编码方式，确保序列化和反序列化的时候都使用一致的编码
        return entry.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public void serialize(String entry, byte[] bytes, int offset, int length) {
        byte[] strBytes = entry.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(strBytes, 0, bytes, offset, strBytes.length);
    }

    @Override
    public String parse(byte[] bytes, int offset, int length) {
        return new String(bytes, offset, length, StandardCharsets.UTF_8);
    }

    @Override
    public byte type() {
        return Types.TYPE_STRING;
    }

    @Override
    public Class<String> getSerializeClass() {
        return String.class;
    }
}
