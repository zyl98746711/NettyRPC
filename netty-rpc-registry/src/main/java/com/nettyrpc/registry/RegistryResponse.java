package com.nettyrpc.registry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

public class RegistryResponse implements Serializable {
    private String requestId;
    private Object result;
    private Exception error;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return error == null;
    }
}