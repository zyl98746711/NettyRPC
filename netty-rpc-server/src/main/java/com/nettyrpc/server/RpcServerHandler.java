package com.nettyrpc.server;

import com.nettyrpc.common.RpcRequest;
import com.nettyrpc.common.RpcResponse;
import com.nettyrpc.serialization.SerializerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;

public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] bytes = (byte[]) msg;
        RpcRequest request = SerializerFactory.getDefaultSerializer().deserialize(bytes, RpcRequest.class);

        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());

        try {
            Object service = ServiceManager.getService(request.getServiceName());
            if (service == null) {
                throw new Exception("Service not found: " + request.getServiceName());
            }

            Method method = service.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
            Object result = method.invoke(service, request.getParameters());
            response.setResult(result);
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