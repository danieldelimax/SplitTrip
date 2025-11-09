package com.sasaki.splittrip;

import com.sasaki.splittrip.models.Expense;

/**
 * Interface de callback para notificar a Activity quando uma nova despesa é criada.
 */
public interface ExpenseCreationListener {
    void onExpenseCreated(Expense newExpense);
}