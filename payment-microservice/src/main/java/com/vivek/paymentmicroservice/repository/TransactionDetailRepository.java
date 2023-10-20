package com.vivek.paymentmicroservice.repository;

import com.vivek.paymentmicroservice.entity.TransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionDetailRepository extends JpaRepository<TransactionDetail,Long> {
    TransactionDetail findByOrderId(Long orderId);
}
