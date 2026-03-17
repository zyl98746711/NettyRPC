package com.nettyrpc.registry;

import com.nettyrpc.common.ServiceRegistry;

public class NetworkRegistryService implements RegistryService {
    private RegistryClient client;

    public NetworkRegistryService(String host, int port) {
        this.client = new RegistryClient(host, port);
        try {
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void register(ServiceRegistry serviceRegistry) throws Exception {
        client.register(serviceRegistry);
    }

    @Override
    public ServiceRegistry discover(String serviceName) throws Exception {
        return client.discover(serviceName);
    }

    @Override
    public void unregister(String serviceName) throws Exception {
        client.unregister(serviceName);
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
