// Localização: com.sasaki.splittrip.adapters
package com.sasaki.splittrip.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sasaki.splittrip.R;
import com.sasaki.splittrip.models.Expense;
import com.sasaki.splittrip.models.Member;

import java.util.List;

/**
 * Adapter para exibir a lista de despesas em uma RecyclerView.
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final List<Expense> expenseList;
    private final List<Member> memberList; // Lista de membros para identificar o pagador
    private final OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
    }

    public ExpenseAdapter(List<Expense> expenseList, List<Member> memberList, OnExpenseClickListener listener) {
        this.expenseList = expenseList;
        this.memberList = memberList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout: expense_list_item.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_list_item, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense currentExpense = expenseList.get(position);

        // 1. Descrição (Adiciona tag de Viagem se for o caso)
        String description = currentExpense.getDescription();
        if (currentExpense.isTripExpense()) {
            description = "[VIAGEM] " + description;
        }
        holder.description.setText(description);

        // 2. Valor
        // CORREÇÃO: Usa calculateTotalTripCost se for despesa de viagem.
        double amountToDisplay = currentExpense.isTripExpense() ? currentExpense.calculateTotalTripCost() : currentExpense.getAmount();
        String formattedAmount = String.format("R$ %.2f", amountToDisplay);
        holder.amount.setText(formattedAmount);

        // 3. Quem Pagou (Função utilitária para procurar o nome)
        String paidByName = findMemberName(currentExpense.getPaidByMemberId());
        holder.paidBy.setText("Pago por: " + paidByName);

        // 4. Listener de Clique
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExpenseClick(currentExpense);
            }
        });
    }

    /**
     * Busca o nome do membro pelo ID.
     */
    private String findMemberName(String memberId) {
        for (Member member : memberList) {
            if (member.getMemberId().equals(memberId)) {
                return member.getName();
            }
        }
        return "Membro Desconhecido";
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    // ViewHolder
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        public final TextView description;
        public final TextView amount;
        public final TextView paidBy;

        public ExpenseViewHolder(View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.text_expense_description);
            amount = itemView.findViewById(R.id.text_expense_amount);
            paidBy = itemView.findViewById(R.id.text_expense_paid_by);
        }
    }
}