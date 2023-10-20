package com.vivek.ordermicroservice.service.impl;

import com.vivek.ordermicroservice.entity.Order;
import com.vivek.ordermicroservice.exception.CustomException;
import com.vivek.ordermicroservice.external.client.PaymentService;
import com.vivek.ordermicroservice.external.client.ProductService;
import com.vivek.ordermicroservice.external.request.PaymentRequest;
import com.vivek.ordermicroservice.external.response.PaymentResponse;
import com.vivek.ordermicroservice.external.response.ProductResponse;
import com.vivek.ordermicroservice.model.OrderRequest;
import com.vivek.ordermicroservice.model.OrderResponse;
import com.vivek.ordermicroservice.repository.OrderRepository;
import com.vivek.ordermicroservice.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RestTemplate restTemplate;
    @Override
    public long placeOrder(OrderRequest orderRequest) {
        //Order entity -> save the data with order status created
        //Product service -> block products (Reduce the quantity)
        //Payment service -> payment -> success -> COMPLETE, Else CANCELLED
        log.info("Placing order request : {}",orderRequest);

        productService.reduceProductQuantity(orderRequest.getProductId(),orderRequest.getQuantity());
        log.info("Creating order with status CREATED");
        Order order = Order.builder()
                .amount(orderRequest.getTotalAmount())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .build();
        order = orderRepository.save(order);
        log.info("calling payment service to complete the payment");
        PaymentRequest paymentRequest = PaymentRequest.builder()
                        .orderId(order.getId())
                                .paymentMode(orderRequest.getPaymentMode())
                                        .amount(orderRequest.getTotalAmount())
                                                .build();
        String orderStatus = null;
        try{
            paymentService.doPayment(paymentRequest);
            log.info("Payment done successfully. Changing order status to placed.");
            orderStatus = "PLACED";
        }catch(Exception ex){
            log.error("Error occured in payment. Changing order status to payment failed.");
            orderStatus = "PAYMENT_FAILED";
        }
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        log.info("Order placed successfully with order id : {}"+order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetail(Long orderId) {
        log.info("Get order details for order Id : {} ",orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(()->new CustomException("Order not found for the order id : "+orderId
                ,"NOT_FOUND",404));

        log.info("Invoking product microservice to fetch the product for id : {} ",order.getProductId());

        ProductResponse productResponse =
                restTemplate.getForObject(
                        "http://PRODUCT-SERVICE/product/"+order.getProductId(),
                        ProductResponse.class
                );

        log.info("Getting payment information from the payment service");
        PaymentResponse paymentResponse =
                restTemplate.getForObject(
                        "http://PAYMENT-SERVICE/payment/"+order.getId(),
                        PaymentResponse.class
                );

        OrderResponse.ProductDetails productDetails =
                OrderResponse.ProductDetails.builder()
                        .productName(productResponse.getProductName())
                        .productId(productResponse.getProductId())
                        .build();

        OrderResponse.PaymentDetails paymentDetails =
                OrderResponse.PaymentDetails.builder()
                        .paymentId(paymentResponse.getPaymentId())
                        .paymentDate(paymentResponse.getPaymentDate())
                        .paymentMode(paymentResponse.getPaymentMode())
                        .paymentStatus(paymentResponse.getStatus())
                        .build();

        OrderResponse orderResponse =
                OrderResponse.builder()
                        .orderId(order.getId())
                        .orderStatus(order.getOrderStatus())
                        .amount(order.getAmount())
                        .orderDate(order.getOrderDate())
                        .productDetails(productDetails)
                        .paymentDetails(paymentDetails)
                        .build();
        return orderResponse;
    }
}
