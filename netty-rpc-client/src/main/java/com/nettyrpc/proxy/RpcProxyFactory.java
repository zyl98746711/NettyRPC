package com.nettyrpc.proxy;

import com.nettyrpc.client.ConnectionPool;
import com.nettyrpc.client.RpcClient;
import com.nettyrpc.common.RpcRequest;
import com.nettyrpc.common.RpcResponse;
import com.nettyrpc.common.ServiceRegistry;
import com.nettyrpc.registry.NetworkRegistryService;
import com.nettyrpc.registry.RegistryFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RpcProxyFactory {
    private ConnectionPool connectionPool;
    private String serviceName;
    private static final ConcurrentHashMap<Class<?>, Object> proxyCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ServiceRegistry> serviceCache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRY_TIME = TimeUnit.MINUTES.toMillis(1); // 缓存过期时间：1分钟
    private static final ConcurrentHashMap<String, Long> serviceCacheTimestamps = new ConcurrentHashMap<>();

    public RpcProxyFactory() {
    }

    public RpcProxyFactory(String host, int port) {
        this.connectionPool = new ConnectionPool(host, port, 10);
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> serviceInterface) {
        // 先从缓存中获取代理实例
        if (proxyCache.containsKey(serviceInterface)) {
            return (T) proxyCache.get(serviceInterface);
        }

        this.serviceName = serviceInterface.getName();
        
        // 检查是否有RpcService注解
        RpcService rpcService = serviceInterface.getAnnotation(RpcService.class);
        if (rpcService != null && connectionPool == null) {
            String host = rpcService.host();
            int port = rpcService.port();
            connectionPool = new ConnectionPool(host, port, 50); // 增加连接池大小
        }

        // 从注册中心获取服务信息
        if (connectionPool == null) {
            // 使用网络注册中心
            RegistryFactory.setRegistryService(new NetworkRegistryService("localhost", 8899));
            ServiceRegistry serviceRegistry = null;
            try {
                serviceRegistry = RegistryFactory.getRegistryService().discover(serviceName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (serviceRegistry != null) {
                String host = serviceRegistry.getHost();
                int port = serviceRegistry.getPort();
                connectionPool = new ConnectionPool(host, port, 50); // 增加连接池大小
            }
        }

        if (connectionPool == null) {
            throw new IllegalArgumentException("Connection pool not initialized. Please either use @RpcService annotation, provide host and port in constructor, or register the service with registry.");
        }

        // 创建新的代理实例
        T proxy = (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RpcClient client = null;
                        try {
                            // 每次调用前都从注册中心获取服务信息，支持负载均衡
                            if (serviceName != null) {
                                // 检查服务信息缓存是否有效
                                long currentTime = System.currentTimeMillis();
                                Long timestamp = serviceCacheTimestamps.get(serviceName);
                                ServiceRegistry serviceRegistry = serviceCache.get(serviceName);
                                
                                // 如果缓存不存在或已过期，则从注册中心获取
                                if (serviceRegistry == null || timestamp == null || currentTime - timestamp > CACHE_EXPIRY_TIME) {
                                    // 使用网络注册中心
                                    RegistryFactory.setRegistryService(new NetworkRegistryService("localhost", 8899));
                                    serviceRegistry = RegistryFactory.getRegistryService().discover(serviceName);
                                    if (serviceRegistry != null) {
                                        // 更新缓存
                                        serviceCache.put(serviceName, serviceRegistry);
                                        serviceCacheTimestamps.put(serviceName, currentTime);
                                    }
                                }
                                
                                if (serviceRegistry != null) {
                                    String host = serviceRegistry.getHost();
                                    int port = serviceRegistry.getPort();
                                    // 如果服务地址发生变化，重新初始化连接池
                                    if (!connectionPool.getHost().equals(host) || connectionPool.getPort() != port) {
                                        connectionPool.close();
                                        connectionPool = new ConnectionPool(host, port, 50); // 增加连接池大小
                                    }
                                }
                            }

                            client = connectionPool.getConnection();
                            // 移除打印，减少日志输出
                            // System.out.println("Sending request to " + connectionPool.getHost() + ":" + connectionPool.getPort());
                            RpcRequest request = new RpcRequest();
                            request.setServiceName(serviceInterface.getName());
                            request.setMethodName(method.getName());
                            request.setParameterTypes(method.getParameterTypes());
                            request.setParameters(args);

                            RpcResponse response = client.sendRequest(request);
                            if (!response.isSuccess()) {
                                throw response.getError();
                            }

                            return response.getResult();
                        } finally {
                            if (client != null) {
                                connectionPool.returnConnection(client);
                            }
                        }
                    }
                }
        );

        // 缓存代理实例
        proxyCache.put(serviceInterface, proxy);
        return proxy;
    }

    public void close() {
        if (connectionPool != null) {
            connectionPool.close();
        }
    }
}