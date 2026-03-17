package com.nettyrpc.client;

import com.nettyrpc.common.RpcRequest;
import com.nettyrpc.common.RpcResponse;
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
import java.util.concurrent.TimeUnit;

public class RpcClient {
    private final String host;
    private final int port;
    private io.netty.channel.Channel channel;
    private EventLoopGroup group;

    public RpcClient(String host, int port) {
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
                                    .addLast(new RpcClientHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();
      //     System.out.println("RPC Client connected to " + host + ":" + port);
        } catch (Exception e) {
            e.printStackTrace();
            if (group != null) {
                group.shutdownGracefully();
            }
            throw e;
        }
    }

    public RpcResponse sendRequest(RpcRequest request) throws Exception {
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
        RpcResponse response = null;

        while (count < timeout / interval) {
            response = RpcClientHandler.getResponse(requestId);
            if (response != null) {
                break;
            }
            TimeUnit.MILLISECONDS.sleep(interval);
            count++;
        }

        if (response == null) {
            throw new Exception("RPC request timeout");
        }

        return response;
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}