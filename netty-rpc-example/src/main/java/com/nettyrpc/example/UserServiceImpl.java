package com.nettyrpc.example;

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