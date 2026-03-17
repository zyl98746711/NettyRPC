# NettyRPC

基于Java 11、Netty和Jackson实现的轻量级RPC框架。

## 技术栈
- Java 11
- Netty 4.1.97.Final
- Jackson 2.15.2
- Maven 3.9+

## 项目结构
```
netty-rpc/
├── src/main/java/
│   ├── com/nettyrpc/
│   │   ├── common/          # 通用模型和工具类
│   │   ├── serialization/    # 序列化实现
│   │   ├── server/           # 服务端实现
│   │   ├── client/           # 客户端实现
│   │   ├── registry/         # 服务注册与发现
│   │   ├── proxy/            # 代理实现
│   │   └── example/          # 示例代码
├── src/test/java/            # 测试代码
├── pom.xml                   # Maven配置
└── README.md                 # 项目说明
```

## 快速开始

### 1. 构建项目
```bash
mvn clean install
```

### 2. 启动服务端
运行 `com.nettyrpc.example.ServerMain` 类，启动RPC服务器。

### 3. 运行客户端
运行 `com.nettyrpc.example.ClientMain` 类，调用远程服务。

## 使用方法

### 服务端
1. 定义服务接口
```java
public interface UserService {
    String getUser(String id);
    int add(int a, int b);
}
```

2. 实现服务接口
```java
public class UserServiceImpl implements UserService {
    @Override
    public String getUser(String id) {
        return "User " + id;
    }

    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
```

3. 注册服务并启动服务器
```java
// 注册服务
RpcServer.registerService(UserService.class.getName(), new UserServiceImpl());

// 启动服务端
RpcServer server = new RpcServer(8888);
server.start();
```

### 客户端
1. 创建代理工厂
```java
RpcProxyFactory proxyFactory = new RpcProxyFactory("localhost", 8888);
```

2. 创建服务代理
```java
UserService userService = proxyFactory.create(UserService.class);
```

3. 调用远程方法
```java
String user = userService.getUser("1001");
int sum = userService.add(10, 20);
```

4. 关闭代理工厂
```java
proxyFactory.close();
```

## 特性
- 基于Netty实现的高性能网络通信
- 使用Jackson进行序列化和反序列化
- 支持服务注册与发现
- 支持动态代理调用
- 实现了连接池管理，提高性能
- 完善的异常处理机制

## 性能优化
- 连接池管理：复用连接，减少连接建立和关闭的开销
- 异步通信：基于Netty的异步非阻塞通信模型
- 序列化优化：使用Jackson高效序列化

## 注意事项
- 服务接口必须是public的
- 方法参数和返回值必须是可序列化的
- 服务端和客户端的服务接口必须保持一致
