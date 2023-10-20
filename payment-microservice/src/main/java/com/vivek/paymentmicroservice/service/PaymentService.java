package com.vivek.paymentmicroservice.service;

import com.vivek.paymentmicroservice.model.PaymentRequest;
import com.vivek.paymentmicroservice.model.PaymentResponse;

public interface PaymentService {
    Long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailByOrderId(String orderId);
}
