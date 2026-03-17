package com.nettyrpc.server;

import com.nettyrpc.common.ServiceRegistry;
import com.nettyrpc.registry.NetworkRegistryService;
import com.nettyrpc.registry.RegistryFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

import java.net.InetAddress;

public class RpcServer {
    private final int port;

    public RpcServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4))
                                    .addLast(new LengthFieldPrepender(4))
                                    .addLast(new ByteArrayDecoder())
                                    .addLast(new ByteArrayEncoder())
                                    .addLast(new RpcServerHandler());
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("RPC Server started on port " + port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void registerService(String serviceName, Object serviceImpl, int port) {
        ServiceManager.registerService(serviceName, serviceImpl);
        
        // 注册到注册中心
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            ServiceRegistry registry = new ServiceRegistry();
            registry.setServiceName(serviceName);
            registry.setServiceImpl(serviceImpl.getClass().getName());
            registry.setHost(host);
            registry.setPort(port);
            
            // 使用网络注册中心
            RegistryFactory.setRegistryService(new NetworkRegistryService("localhost", 8899));
            RegistryFactory.getRegistryService().register(registry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}