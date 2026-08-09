package com.clearledger.debt_service.strategy;

import com.clearledger.debt_service.entity.Debt;
import java.util.Comparator;
import java.util.List;

public class AvalancheStrategy implements DebtPayoffStrategy {

    @Override
    public List<Debt> sort(List<Debt> debts) {
        // Highest interest rate first — minimises total interest paid
        return debts.stream()
                .sorted(Comparator.comparing(Debt::getInterestRate).reversed())
                .toList();
    }

    @Override
    public String getReason(Debt debt) {
        return "Highest interest rate (" + debt.getInterestRate() + "%) — attack first to minimise total interest";
    }
}

//Notice the only difference between these two classes is one line — the Comparator.
// That's the whole point of the Strategy Pattern. All the complex payoff simulation logic lives
// once in DebtService and delegates the sort to whichever strategy was injected.