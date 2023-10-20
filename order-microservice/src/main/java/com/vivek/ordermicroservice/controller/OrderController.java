package com.vivek.ordermicroservice.controller;

import com.vivek.ordermicroservice.model.OrderRequest;
import com.vivek.ordermicroservice.model.OrderResponse;
import com.vivek.ordermicroservice.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Log4j2
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/place_order")
    public ResponseEntity<Long> placeOrder(@RequestBody OrderRequest orderRequest){
        long orderId = orderService.placeOrder(orderRequest);
        log.info("Order id : {}",orderId);
        return new ResponseEntity<>(orderId, HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable Long orderId){
        OrderResponse orderResponse = orderService.getOrderDetail(orderId);
        return new ResponseEntity<>(orderResponse,HttpStatus.OK);
    }
}
