package com.nettyrpc.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {
    private final BlockingQueue<RpcClient> pool;
    private final String host;
    private final int port;
    private final int poolSize;

    public ConnectionPool(String host, int port, int poolSize) {
        this.host = host;
        this.port = port;
        this.poolSize = poolSize;
        this.pool = new LinkedBlockingQueue<>(poolSize);

        // 初始化连接池
        for (int i = 0; i < poolSize; i++) {
            try {
                RpcClient client = new RpcClient(host, port);
                client.connect();
                pool.offer(client);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public RpcClient getConnection() throws Exception {
        RpcClient client = pool.poll(5000, TimeUnit.MILLISECONDS);
        if (client == null) {
            throw new Exception("No available connection in pool");
        }
        return client;
    }

    public void returnConnection(RpcClient client) {
        if (client != null) {
            pool.offer(client);
        }
    }

    public void close() {
        while (!pool.isEmpty()) {
            RpcClient client = pool.poll();
            if (client != null) {
                client.close();
            }
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}