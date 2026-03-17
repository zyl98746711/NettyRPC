package com.nettyrpc.client;

import com.nettyrpc.common.RpcResponse;
import com.nettyrpc.serialization.SerializerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ConcurrentHashMap;

public class RpcClientHandler extends ChannelInboundHandlerAdapter {
    private static final ConcurrentHashMap<String, RpcResponse> responseMap = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] bytes = (byte[]) msg;
        RpcResponse response = SerializerFactory.getDefaultSerializer().deserialize(bytes, RpcResponse.class);
        responseMap.put(response.getRequestId(), response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public static RpcResponse getResponse(String requestId) {
        return responseMap.remove(requestId);
    }
}