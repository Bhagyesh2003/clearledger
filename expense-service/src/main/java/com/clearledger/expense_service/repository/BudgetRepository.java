package com.clearledger.expense_service.repository;

import com.clearledger.expense_service.entity.Budget;
import com.clearledger.expense_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, String> {

    List<Budget> findByUserIdAndMonthAndYear(String userId, int month, int year);

    Optional<Budget> findByUserIdAndCategoryAndMonthAndYear(
            String userId, Category category, int month, int year
    );
}