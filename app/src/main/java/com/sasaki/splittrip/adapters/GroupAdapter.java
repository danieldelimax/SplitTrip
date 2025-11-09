// Localização: com.sasaki.splittrip.adapters
package com.sasaki.splittrip.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sasaki.splittrip.R; // Necessário para acessar os layouts e IDs
import com.sasaki.splittrip.models.Group;

import java.util.List;

/**
 * Adapter para exibir os grupos de viagem na RecyclerView da MainActivity.
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private final List<Group> groupList;
    // CORREÇÃO: Renomeado para GroupClickListener (para sincronizar com MainActivity)
    private final GroupClickListener listener;

    // CORREÇÃO: Interface GroupClickListener e método com 'position'
    public interface GroupClickListener {
        void onGroupClick(Group group, int position); // Adicionado 'int position'
    }

    public GroupAdapter(List<Group> groupList, GroupClickListener listener) {
        this.groupList = groupList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout: group_list_item.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_list_item, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group currentGroup = groupList.get(position);

        // Preenche os dados
        holder.groupTitle.setText(currentGroup.getGroupName());

        String membersCount = currentGroup.getMembers().size() + " Membros";
        holder.groupMembersCount.setText(membersCount);

        // Exibe o total de gastos (usando o método de cálculo do POJO)
        String totalSpent = String.format("R$ %.2f", currentGroup.calculateTotalExpenses());
        holder.totalSpentValue.setText(totalSpent);

        // Define o Listener de clique
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // CORREÇÃO: Passa a posição e o grupo para o listener
                listener.onGroupClick(currentGroup, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    // View Holder
    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        public final TextView groupTitle;
        public final TextView groupMembersCount;
        public final TextView totalSpentValue;

        public GroupViewHolder(View itemView) {
            super(itemView);
            groupTitle = itemView.findViewById(R.id.text_group_title);
            groupMembersCount = itemView.findViewById(R.id.text_group_members_count);
            totalSpentValue = itemView.findViewById(R.id.text_total_spent_value);
        }
    }
}