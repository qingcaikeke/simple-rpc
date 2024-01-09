package com.yjy.server;

public interface ServiceProviderRegistry {
    <T> void addServiceProvider(Class<? extends T> serviceClass, T serviceProvider);
}
