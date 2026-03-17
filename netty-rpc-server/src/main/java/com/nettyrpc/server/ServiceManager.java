package com.nettyrpc.server;

import java.util.concurrent.ConcurrentHashMap;

public class ServiceManager {
    private static final ConcurrentHashMap<String, Object> serviceMap = new ConcurrentHashMap<>();

    public static void registerService(String serviceName, Object serviceImpl) {
        serviceMap.put(serviceName, serviceImpl);
    }

    public static Object getService(String serviceName) {
        return serviceMap.get(serviceName);
    }

    public static boolean containsService(String serviceName) {
        return serviceMap.containsKey(serviceName);
    }
}