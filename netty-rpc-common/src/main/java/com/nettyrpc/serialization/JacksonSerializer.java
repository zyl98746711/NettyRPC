package com.nettyrpc.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonSerializer implements Serializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T obj) throws Exception {
        return objectMapper.writeValueAsBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        return objectMapper.readValue(bytes, clazz);
    }
}