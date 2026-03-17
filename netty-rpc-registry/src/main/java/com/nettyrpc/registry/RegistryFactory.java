package com.nettyrpc.registry;

public class RegistryFactory {
    private static RegistryService registryService;

    static {
        // 默认使用本地注册中心
        registryService = new LocalRegistryService();
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService service) {
        registryService = service;
    }
}