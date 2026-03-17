package com.nettyrpc.registry;

import com.nettyrpc.common.ServiceRegistry;
import com.nettyrpc.serialization.SerializerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

public class RegistryServerHandler extends ChannelInboundHandlerAdapter {
    private static final Map<String, Object> registryMap = new HashMap<>();
    private static final LocalRegistryService registryService = new LocalRegistryService();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] bytes = (byte[]) msg;
        RegistryRequest request = SerializerFactory.getDefaultSerializer().deserialize(bytes, RegistryRequest.class);

        RegistryResponse response = new RegistryResponse();
        response.setRequestId(request.getRequestId());

        try {
            switch (request.getType()) {
                case REGISTER:
                    Object data = request.getData();
                    ServiceRegistry serviceRegistry;
                    if (data instanceof java.util.Map) {
                        // 处理Jackson反序列化的Map对象
                        java.util.Map<?, ?> map = (java.util.Map<?, ?>) data;
                        serviceRegistry = new com.nettyrpc.common.ServiceRegistry();
                        serviceRegistry.setServiceName((String) map.get("serviceName"));
                        serviceRegistry.setServiceImpl((String) map.get("serviceImpl"));
                        serviceRegistry.setHost((String) map.get("host"));
                        serviceRegistry.setPort((Integer) map.get("port"));
                    } else {
                        serviceRegistry = (ServiceRegistry) data;
                    }
                    registryService.register(serviceRegistry);
                    response.setResult("SUCCESS");
                    break;
                case DISCOVER:
                    String serviceName = (String) request.getData();
                    ServiceRegistry discoveredService = registryService.discover(serviceName);
                    response.setResult(discoveredService);
                    break;
                case UNREGISTER:
                    String unregisterServiceName = (String) request.getData();
                    registryService.unregister(unregisterServiceName);
                    response.setResult("SUCCESS");
                    break;
                default:
                    response.setError(new Exception("Unknown request type: " + request.getType()));
            }
        } catch (Exception e) {
            response.setError(e);
        }

        byte[] responseBytes = SerializerFactory.getDefaultSerializer().serialize(response);
        ctx.writeAndFlush(responseBytes);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}