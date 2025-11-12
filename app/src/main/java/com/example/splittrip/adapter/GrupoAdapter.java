package com.example.splittrip.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.splittrip.MainActivity;
import com.example.splittrip.OnGroupActionListener;
import com.example.splittrip.R;
import com.example.splittrip.model.GrupoViagem;

public class GrupoAdapter extends RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder> {

    private List<GrupoViagem> grupos;
    private final OnGroupActionListener listener;

    public GrupoAdapter(List<GrupoViagem> grupos, OnGroupActionListener listener) {
        this.grupos = grupos;
        this.listener = listener;
    }

    //Atualiza a lista de grupos, útil após carregar dados ou realizar exclusões
    public void setGroups(List<GrupoViagem> newGroups) {
        this.grupos = newGroups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GrupoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GrupoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrupoViewHolder holder, int position) {
        GrupoViagem grupo = grupos.get(position);

        //Nome do Grupo
        holder.tvGroupName.setText(grupo.getNome());

        //Detalhes (Membros, Distância, Pedágio)
        String details = String.format("%d Membros | %.0f km | R$ %.2f Pedágio",
                grupo.getNumeroParticipantes(),
                grupo.getDistanciaKm(),
                grupo.getPedagio());
        holder.tvGroupDetails.setText(details);

        //Clique no Card (Abre CostsActivity para cálculo)
        holder.itemView.setOnClickListener(v -> {
            listener.onGroupClick(grupo, position);
        });

        //Clique no Botão de Editar (Abre Diálogo na MainActivity)
        holder.btnEdit.setOnClickListener(v -> {
            listener.onGroupEdit(grupo, position);
        });

        //Clique Longo (Sugestão para exclusão rápida)
        holder.itemView.setOnLongClickListener(v -> {
            ((MainActivity) holder.itemView.getContext()).deleteGroup(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return grupos.size();
    }

    //ViewHolder para os itens do RecyclerView
    public static class GrupoViewHolder extends RecyclerView.ViewHolder {
        final TextView tvGroupName; //TextView
        final TextView tvGroupDetails;
        final ImageButton btnEdit;  //Button (ImageButton)

        public GrupoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvGroupDetails = itemView.findViewById(R.id.tvGroupDetails);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}