package com.nettyrpc.registry;

import com.nettyrpc.common.ServiceRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LocalRegistryService implements RegistryService {
    private final ConcurrentHashMap<String, List<ServiceRegistry>> registryMap = new ConcurrentHashMap<>();

    @Override
    public void register(ServiceRegistry serviceRegistry) throws Exception {
        String serviceName = serviceRegistry.getServiceName();
        registryMap.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(serviceRegistry);
        System.out.println("Service registered: " + serviceName + " at " + serviceRegistry.getHost() + ":" + serviceRegistry.getPort());
    }

    @Override
    public ServiceRegistry discover(String serviceName) throws Exception {
        List<ServiceRegistry> services = registryMap.get(serviceName);
        if (services == null || services.isEmpty()) {
            return null;
        }
        // 使用随机负载均衡策略
        LoadBalance loadBalance = new RandomLoadBalance();
        return loadBalance.select(services);
    }

    @Override
    public void unregister(String serviceName) throws Exception {
        registryMap.remove(serviceName);
        System.out.println("Service unregistered: " + serviceName);
    }

    @Override
    public void close() {
        registryMap.clear();
    }

    public List<ServiceRegistry> getServices(String serviceName) {
        return registryMap.get(serviceName);
    }
}