package com.clearledger.expense_service.repository;

import com.clearledger.expense_service.entity.Category;
import com.clearledger.expense_service.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, String> {

    List<Expense> findByUserIdOrderBySpentOnDesc(String userId);

    List<Expense> findByUserIdAndCategoryOrderBySpentOnDesc(String userId, Category category);

    // Total spent per category for a given month — used to calculate budget usage
    @Query("""
        SELECT e.category, SUM(e.amount)
        FROM Expense e
        WHERE e.userId = :userId
          AND EXTRACT(MONTH FROM e.spentOn) = :month
          AND EXTRACT(YEAR  FROM e.spentOn) = :year
        GROUP BY e.category
    """)
    List<Object[]> findMonthlySpendingByCategory(
            @Param("userId") String userId,
            @Param("month") int month,
            @Param("year") int year
    );
}