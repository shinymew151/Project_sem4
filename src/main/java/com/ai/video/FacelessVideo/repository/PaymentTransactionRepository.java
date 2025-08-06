package com.ai.video.FacelessVideo.repository;

import com.ai.video.FacelessVideo.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
    List<PaymentTransaction> findByUserId(String userId);
    List<PaymentTransaction> findByStatus(String status);
    Optional<PaymentTransaction> findByTransactionRef(String transactionRef);
}