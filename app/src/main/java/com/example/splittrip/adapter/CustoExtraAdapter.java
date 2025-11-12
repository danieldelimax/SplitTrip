package com.example.splittrip.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.splittrip.R;
import com.example.splittrip.model.CustoExtra;

public class CustoExtraAdapter extends RecyclerView.Adapter<CustoExtraAdapter.CostViewHolder> {

    private List<CustoExtra> custos;
    private final OnCostDeleteListener deleteListener;

    //Interface de callback para exclusão
    public interface OnCostDeleteListener {
        void onCostDeleted(int position);
    }

    public CustoExtraAdapter(List<CustoExtra> custos, OnCostDeleteListener deleteListener) {
        this.custos = custos;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public CostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cost, parent, false);
        return new CostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CostViewHolder holder, int position) {
        CustoExtra custo = custos.get(position);

        //Descrição do gasto
        holder.tvCostDescription.setText(custo.getDescricao());

        // Valor e Moeda (Ex: USD 150.00)
        String valueText = String.format("%s %.2f", custo.getMoeda(), custo.getValor());
        holder.tvCostValue.setText(valueText);

        //Ação de Excluir
        holder.btnDeleteCost.setOnClickListener(v -> {
            //Notifica a Activity (CostsActivity) para remover o item e atualizar o cálculo
            deleteListener.onCostDeleted(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return custos.size();
    }

    public static class CostViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCostDescription;
        final TextView tvCostValue;
        final ImageButton btnDeleteCost;

        public CostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCostDescription = itemView.findViewById(R.id.tvCostDescription);
            tvCostValue = itemView.findViewById(R.id.tvCostValue);
            btnDeleteCost = itemView.findViewById(R.id.btnDeleteCost);
        }
    }
}