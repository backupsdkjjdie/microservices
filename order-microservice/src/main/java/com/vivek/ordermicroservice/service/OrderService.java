package com.vivek.ordermicroservice.service;

import com.vivek.ordermicroservice.model.OrderRequest;
import com.vivek.ordermicroservice.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetail(Long orderId);
}
