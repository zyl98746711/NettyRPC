package com.nettyrpc.registry;

import com.nettyrpc.common.ServiceRegistry;
import com.nettyrpc.serialization.SerializerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RegistryClient {
    private final String host;
    private final int port;
    private io.netty.channel.Channel channel;
    private EventLoopGroup group;
    private static final ConcurrentHashMap<String, RegistryResponse> responseMap = new ConcurrentHashMap<>();

    public RegistryClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws Exception {
        group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4))
                                    .addLast(new LengthFieldPrepender(4))
                                    .addLast(new ByteArrayDecoder())
                                    .addLast(new ByteArrayEncoder())
                                    .addLast(new RegistryClientHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();
   //         System.out.println("Registry Client connected to " + host + ":" + port);
        } catch (Exception e) {
            e.printStackTrace();
            if (group != null) {
                group.shutdownGracefully();
            }
            throw e;
        }
    }

    public RegistryResponse sendRequest(RegistryRequest request) throws Exception {
        if (channel == null || !channel.isActive()) {
            connect();
        }

        String requestId = UUID.randomUUID().toString();
        request.setRequestId(requestId);

        byte[] bytes = SerializerFactory.getDefaultSerializer().serialize(request);
        channel.writeAndFlush(bytes);

        // 等待响应
        int timeout = 5000;
        int interval = 100;
        int count = 0;
        RegistryResponse response = null;

        while (count < timeout / interval) {
            response = responseMap.remove(requestId);
            if (response != null) {
                break;
            }
            TimeUnit.MILLISECONDS.sleep(interval);
            count++;
        }

        if (response == null) {
            throw new Exception("Registry request timeout");
        }

        return response;
    }

    public void register(ServiceRegistry serviceRegistry) throws Exception {
        RegistryRequest request = new RegistryRequest();
        request.setType(RegistryRequest.RequestType.REGISTER);
        request.setData(serviceRegistry);
        RegistryResponse response = sendRequest(request);
        if (!response.isSuccess()) {
            throw response.getError();
        }
    }

    public ServiceRegistry discover(String serviceName) throws Exception {
        RegistryRequest request = new RegistryRequest();
        request.setType(RegistryRequest.RequestType.DISCOVER);
        request.setData(serviceName);
        RegistryResponse response = sendRequest(request);
        if (!response.isSuccess()) {
            throw response.getError();
        }
        Object result = response.getResult();
        if (result instanceof java.util.Map) {
            // 处理Jackson反序列化的Map对象
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) result;
            ServiceRegistry serviceRegistry = new ServiceRegistry();
            serviceRegistry.setServiceName((String) map.get("serviceName"));
            serviceRegistry.setServiceImpl((String) map.get("serviceImpl"));
            serviceRegistry.setHost((String) map.get("host"));
            serviceRegistry.setPort((Integer) map.get("port"));
            return serviceRegistry;
        } else {
            return (ServiceRegistry) result;
        }
    }

    public void unregister(String serviceName) throws Exception {
        RegistryRequest request = new RegistryRequest();
        request.setType(RegistryRequest.RequestType.UNREGISTER);
        request.setData(serviceName);
        RegistryResponse response = sendRequest(request);
        if (!response.isSuccess()) {
            throw response.getError();
        }
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    public static void addResponse(String requestId, RegistryResponse response) {
        responseMap.put(requestId, response);
    }
}