package com.nettyrpc.registry;

import java.io.Serializable;

public class RegistryRequest implements Serializable {
    private String requestId;
    private RequestType type;
    private Object data;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public enum RequestType {
        REGISTER,
        DISCOVER,
        UNREGISTER
    }
}