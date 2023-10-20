package com.vivek.productmicroservice.service;

import com.vivek.productmicroservice.model.ProductRequest;
import com.vivek.productmicroservice.model.ProductResponse;

public interface ProductService {
    long addProduct(ProductRequest productRequest);

    ProductResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);
}
