package com.nettyrpc.registry;

import com.nettyrpc.common.ServiceRegistry;
import java.util.List;

public interface LoadBalance {
    ServiceRegistry select(List<ServiceRegistry> services);
}