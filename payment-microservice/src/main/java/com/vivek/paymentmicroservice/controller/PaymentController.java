package com.vivek.paymentmicroservice.controller;

import com.vivek.paymentmicroservice.model.PaymentRequest;
import com.vivek.paymentmicroservice.model.PaymentResponse;
import com.vivek.paymentmicroservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest){
        return new ResponseEntity<>(paymentService.doPayment(paymentRequest), HttpStatus.OK);
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentDetailByOrderId(@PathVariable String orderId){
        return new ResponseEntity<>(
                paymentService.getPaymentDetailByOrderId(orderId),
                HttpStatus.OK
        );
    }
}
