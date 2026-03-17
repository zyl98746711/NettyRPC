package com.nettyrpc.registry;

import com.nettyrpc.common.ServiceRegistry;
import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance {
    private static final Random random = new Random();

    @Override
    public ServiceRegistry select(List<ServiceRegistry> services) {
        if (services == null || services.isEmpty()) {
            return null;
        }
        if (services.size() == 1) {
            return services.get(0);
        }
        return services.get(random.nextInt(services.size()));
    }
}