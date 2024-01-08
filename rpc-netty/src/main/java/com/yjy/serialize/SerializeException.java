package com.yjy.serialize;

public class SerializeException extends RuntimeException {
    public SerializeException(String msg) {
        super(msg);
    }
    public SerializeException(Throwable throwable){ super(throwable);}

}
