package com.nettyrpc.example;

import com.nettyrpc.server.RpcServer;

public class ServerMain2 {
    public static void main(String[] args) throws Exception {
        int port = 8889;
        // 注册服务
        RpcServer.registerService(UserService.class.getName(), new UserServiceImpl(), port);

        // 启动服务端
        RpcServer server = new RpcServer(port);
        server.start();
    }
}
