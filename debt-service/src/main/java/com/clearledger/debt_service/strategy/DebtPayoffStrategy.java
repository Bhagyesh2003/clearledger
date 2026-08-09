package com.clearledger.debt_service.strategy;

import com.clearledger.debt_service.entity.Debt;
import java.util.List;

public interface DebtPayoffStrategy {

    // Each implementation sorts debts in the order they should be attacked
    List<Debt> sort(List<Debt> debts);

    // Human-readable reason why a debt was prioritised — shown in the response
    String getReason(Debt debt);
}

//This is an interface defining the contract for different debt payoff strategies.
// Each strategy will implement the sort method to order debts according to its logic (e.g., Snowball or Avalanche)
// and provide a reason for prioritizing each debt.