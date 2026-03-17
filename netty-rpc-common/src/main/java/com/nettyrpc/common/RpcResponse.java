package com.nettyrpc.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

public class RpcResponse implements Serializable {
    private String requestId;
    private Object result;
    private Throwable error;

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

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return error == null;
    }
}