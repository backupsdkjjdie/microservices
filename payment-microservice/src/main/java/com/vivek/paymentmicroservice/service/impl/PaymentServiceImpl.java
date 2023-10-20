package com.vivek.paymentmicroservice.service.impl;

import com.vivek.paymentmicroservice.entity.TransactionDetail;
import com.vivek.paymentmicroservice.model.PaymentMode;
import com.vivek.paymentmicroservice.model.PaymentRequest;
import com.vivek.paymentmicroservice.model.PaymentResponse;
import com.vivek.paymentmicroservice.repository.TransactionDetailRepository;
import com.vivek.paymentmicroservice.service.PaymentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private TransactionDetailRepository transactionDetailRepository;
    @Override
    public Long doPayment(PaymentRequest paymentRequest) {
        log.info("Recording payment details : {}",paymentRequest);
        TransactionDetail transactionDetail = TransactionDetail.builder()
                .paymentDate(Instant.now())
                .paymentMode(paymentRequest.getPaymentMode().name())
                .paymentStatus("SUCCESS")
                .orderId(paymentRequest.getOrderId())
                .referenceNuumber(paymentRequest.getReferenceNumber())
                .amount(paymentRequest.getAmount())
                .build();
        transactionDetailRepository.save(transactionDetail);
        log.info("Transaction completed with id : {}",transactionDetail.getId());
        return transactionDetail.getId();
    }

    @Override
    public PaymentResponse getPaymentDetailByOrderId(String orderId) {
        log.info("Getting payment details for the order id : {}",orderId);
        TransactionDetail transactionDetail =
            transactionDetailRepository.findByOrderId(Long.valueOf(orderId));
        PaymentResponse paymentResponse =
                PaymentResponse.builder()
                        .paymentId(transactionDetail.getId())
                        .paymentMode(PaymentMode.valueOf(transactionDetail.getPaymentMode()))
                        .paymentDate(transactionDetail.getPaymentDate())
                        .orderId(transactionDetail.getOrderId())
                        .status(transactionDetail.getPaymentStatus())
                        .amount(transactionDetail.getAmount())
                        .build();
        return paymentResponse;
    }
}

