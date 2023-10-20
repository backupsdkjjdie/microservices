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
import com.vivek.ordermicroservice.model.PaymentMode;
import com.vivek.ordermicroservice.repository.OrderRepository;
import com.vivek.ordermicroservice.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
public class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductService productService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    OrderService orderService = new OrderServiceImpl();
    @DisplayName("Get Order - Success Scenario")
    @Test
    void test_When_Order_Success(){
        //actual call the method
        Order order =getMockOrder();
        ProductResponse productResponse = getMockProductResponse();
        PaymentResponse paymentResponse = getMockPaymentResponse();

        //mock
        Mockito.when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.of(order));

        Mockito.when((restTemplate.getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(), ProductResponse.class)))
                .thenReturn(productResponse);

        Mockito.when(restTemplate.getForObject("http://PAYMENT-SERVICE/payment/"+order.getId(),
                PaymentResponse.class))
                .thenReturn(paymentResponse);

        //Actual
        OrderResponse orderResponse = orderService.getOrderDetail(1l);
        //verification
        Mockito.verify(orderRepository, Mockito.times(1)).findById(anyLong());
        Mockito.verify(restTemplate, Mockito.times(1))
                .getForObject("http://PRODUCT-SERVICE/product/"+order.getProductId(), ProductResponse.class);
        Mockito.verify(restTemplate,Mockito.times(1))
                .getForObject("http://PAYMENT-SERVICE/payment/"+order.getId(),
                        PaymentResponse.class);

        //Assertion
        Assertions.assertNotNull(orderResponse);
        Assertions.assertEquals(order.getId(), orderResponse.getOrderId());

    }

    @DisplayName("Get Orders - Failure Scenario")
    @Test
    void test_when_Get_Order_NOT_FOUND(){
        Mockito.when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(null));

        //Assertion
        CustomException exception = Assertions.assertThrows(CustomException.class,
                () ->  orderService.getOrderDetail(1l));
        Assertions.assertEquals("NOT_FOUND", exception.getErrorCode());
        Assertions.assertEquals(404, exception.getStatus());

        //verification
        Mockito.verify(orderRepository, Mockito.times(1))
                .findById(anyLong());
    }



    @DisplayName("Place Order - Success Scenario")
    @Test
    void test_When_Place_Order_Success(){
        OrderRequest orderRequest = getMockOrderRequest();
        Order order = getMockOrder();
        Mockito.when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        Mockito.when(productService.reduceProductQuantity(anyLong(),anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        Mockito.when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        long orderId = orderService.placeOrder(orderRequest);
        Mockito.verify(orderRepository,Mockito.times(2))
                .save(any());
        Mockito.verify(productService, Mockito.times(1))
                .reduceProductQuantity(anyLong(),anyLong());
        Mockito.verify(paymentService, Mockito.times(1))
                .doPayment(any(PaymentRequest.class));
        Assertions.assertEquals(order.getId(), orderId);
    }

    @DisplayName("Place Order - Payment Failed Scenario")
    @Test
    void test_when_Place_Order_Payment_Fails_then_Order_Placed(){
        OrderRequest orderRequest = getMockOrderRequest();
        Order order = getMockOrder();
        Mockito.when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        Mockito.when(productService.reduceProductQuantity(anyLong(),anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        Mockito.when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException());

        long orderId = orderService.placeOrder(orderRequest);
        Mockito.verify(orderRepository,Mockito.times(2))
                .save(any());
        Mockito.verify(productService, Mockito.times(1))
                .reduceProductQuantity(anyLong(),anyLong());
        Mockito.verify(paymentService, Mockito.times(1))
                .doPayment(any(PaymentRequest.class));
        Assertions.assertEquals(order.getId(), orderId);
    }

    private OrderRequest getMockOrderRequest() {
        return OrderRequest.builder()
                .productId(1)
                .quantity(10)
                .paymentMode(PaymentMode.CASH)
                .totalAmount(100l)
                .build();
    }

    private PaymentResponse getMockPaymentResponse() {
        return PaymentResponse.builder()
                .paymentId(1l)
                .paymentDate(Instant.now())
                .paymentMode(PaymentMode.CASH)
                .amount(200l)
                .orderId(1l)
                .status("ACCEPTED")
                .build();
    }

    private ProductResponse getMockProductResponse() {
        return ProductResponse.builder()
                .productId(2)
                .productName("iPhone")
                .price(100)
                .quantity(200)
                .build();
    }

    private Order getMockOrder() {
        return Order.builder()
                .orderStatus("PLACED")
                .orderDate(Instant.now())
                .id(1)
                .amount(100)
                .quantity(200)
                .productId(2)
                .build();
    }
}