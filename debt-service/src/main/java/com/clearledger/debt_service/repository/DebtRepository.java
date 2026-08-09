package com.clearledger.debt_service.repository;

import com.clearledger.debt_service.entity.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DebtRepository extends JpaRepository<Debt, String> {

    List<Debt> findByUserIdAndPaidFalseOrderByCreatedAtDesc(String userId);

    Optional<Debt> findByIdAndUserId(String id, String userId);

    // Sum of all remaining unpaid debt — called by Net Worth Service
    @Query("SELECT COALESCE(SUM(d.remainingAmount), 0) FROM Debt d WHERE d.userId = :userId AND d.paid = false")
    BigDecimal getTotalOutstanding(@Param("userId") String userId);
}

//getTotalOutstanding is the internal endpoint Net Worth Service will call via REST. COALESCE(..., 0)
// ensures we return 0 instead of null when the user has no debts — null would cause a NullPointerException
// in Net Worth's calculation.