package com.vineelpynam.OrderService;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class TestServiceInstanceListSupplier implements ServiceInstanceListSupplier {
    @Override
    public String getServiceId() {
        return null;
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        List<ServiceInstance> serviceInstanceList = new ArrayList<>();
        serviceInstanceList.add(new DefaultServiceInstance(
                "payment-service-svc",
                "payment-service-svc",
                "localhost",
                8989,
                false
        ));
        serviceInstanceList.add(new DefaultServiceInstance(
                "product-service-svc",
                "product-service-svc",
                "localhost",
                8988,
                false
        ));
        return Flux.just(serviceInstanceList);
    }
}
