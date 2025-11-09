// Localização: com.sasaki.splittrip.adapters
package com.sasaki.splittrip.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sasaki.splittrip.R;
import com.sasaki.splittrip.models.Member;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final List<Member> members;

    public MemberAdapter(List<Member> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout: member_list_item.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_list_item, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = members.get(position);
        holder.memberName.setText(member.getName());
        holder.memberEmail.setText(member.getEmail());

        // CORREÇÃO: Exibição do Saldo e formatação
        double balance = member.getBalance();
        String prefixText;
        int color;

        if (balance > 0.0) {
            // Se o saldo for POSITIVO, ele deve receber dinheiro
            prefixText = "A Receber: ";
            color = Color.rgb(0, 128, 0); // Verde
        } else if (balance < 0.0) {
            // Se o saldo for NEGATIVO, ele deve pagar dinheiro
            prefixText = "Deve: ";
            color = Color.RED;
        } else {
            // Saldo zero
            prefixText = "Tudo Certo: ";
            color = Color.GRAY;
        }

        holder.memberBalance.setText(prefixText + member.getFormattedBalance());
        holder.memberBalance.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        final TextView memberName;
        final TextView memberEmail;
        final TextView memberBalance; // Novo componente para exibir o saldo

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.text_member_name);
            memberEmail = itemView.findViewById(R.id.text_member_email);
            // Assumindo que você adicionou este ID no member_list_item.xml
            memberBalance = itemView.findViewById(R.id.text_member_balance);
        }
    }
}