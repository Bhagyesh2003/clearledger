package com.clearledger.debt_service.strategy;

import com.clearledger.debt_service.entity.Debt;
import java.util.Comparator;
import java.util.List;

public class SnowballStrategy implements DebtPayoffStrategy {

    @Override
    public List<Debt> sort(List<Debt> debts) {
        // Smallest balance first — quickest psychological win
        return debts.stream()
                .sorted(Comparator.comparing(Debt::getRemainingAmount))
                .toList();
    }

    @Override
    public String getReason(Debt debt) {
        return "Lowest remaining balance — pay off first for a quick win";
    }
}