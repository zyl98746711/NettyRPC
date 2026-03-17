package com.nettyrpc.registry;

import com.nettyrpc.common.ServiceRegistry;

public interface RegistryService {
    void register(ServiceRegistry serviceRegistry) throws Exception;
    ServiceRegistry discover(String serviceName) throws Exception;
    void unregister(String serviceName) throws Exception;
    void close();
}