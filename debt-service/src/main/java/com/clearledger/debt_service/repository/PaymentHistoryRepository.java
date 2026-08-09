package com.clearledger.debt_service.repository;

import com.clearledger.debt_service.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, String> {

    List<PaymentHistory> findByDebt_IdOrderByPaidOnDesc(String debtId);
}