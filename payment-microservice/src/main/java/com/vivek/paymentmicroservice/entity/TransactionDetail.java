package com.vivek.paymentmicroservice.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Entity
@Table(name = "Transaction_Detail")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "payment_mode")
    private String paymentMode;
    @Column(name = "reference_number")
    private String referenceNuumber;
    @Column(name = "payment_date")
    private Instant paymentDate;
    @Column(name = "payment_status")
    private String paymentStatus;
    @Column(name = "total_amount")
    private Long amount;
}
