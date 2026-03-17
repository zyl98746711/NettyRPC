package com.nettyrpc.serialization;

public class SerializerFactory {
    private static final Serializer DEFAULT_SERIALIZER = new JacksonSerializer();

    public static Serializer getDefaultSerializer() {
        return DEFAULT_SERIALIZER;
    }

    public static Serializer getSerializer(String type) {
        if ("jackson".equalsIgnoreCase(type)) {
            return new JacksonSerializer();
        }
        return DEFAULT_SERIALIZER;
    }
}