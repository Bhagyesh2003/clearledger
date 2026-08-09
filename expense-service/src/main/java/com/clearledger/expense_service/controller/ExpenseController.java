package com.clearledger.expense_service.controller;

import com.clearledger.expense_service.dto.*;
import com.clearledger.expense_service.entity.Category;
import com.clearledger.expense_service.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // ── Expenses ──────────────────────────────────────────────────────────────

    @PostMapping("/expenses")
    public ResponseEntity<ExpenseResponse> createExpense(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpense(userId, request));
    }

    @GetMapping("/expenses")
    public ResponseEntity<List<ExpenseResponse>> getExpenses(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) Category category) {
        return ResponseEntity.ok(expenseService.getExpenses(userId, category));
    }

    // ── Budgets ───────────────────────────────────────────────────────────────

    @PostMapping("/budgets")
    public ResponseEntity<BudgetResponse> createBudget(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateBudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createBudget(userId, request));
    }

    @GetMapping("/budgets")
    public ResponseEntity<List<BudgetResponse>> getBudgets(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {
        if (month == 0) month = LocalDate.now().getMonthValue();
        if (year == 0) year = LocalDate.now().getYear();
        return ResponseEntity.ok(expenseService.getBudgets(userId, month, year));
    }
}