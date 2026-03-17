package com.nettyrpc.example;

import com.nettyrpc.proxy.RpcProxyFactory;

import java.util.ArrayList;
import java.util.List;


public class ClientMain {
    public static void main(String[] args) throws Exception {
        // 创建代理工厂（使用无参构造函数，连接信息从注册中心获取）
        RpcProxyFactory proxyFactory = new RpcProxyFactory();

        // 创建服务代理
        UserService userService = proxyFactory.create(UserService.class);
        // 线程池 并发调用
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }
        list.parallelStream().forEach(i -> {
            String user = userService.getUser("100" + i);
            System.out.println("Call " + i + " getUser result: " + user);
        });

        // 关闭代理工厂
        proxyFactory.close();
    }
}