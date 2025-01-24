package com.vineelpynam.ProductService.service;

import com.vineelpynam.ProductService.model.ProductRequest;
import com.vineelpynam.ProductService.model.ProductResponse;

public interface ProductService {
    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);
}
