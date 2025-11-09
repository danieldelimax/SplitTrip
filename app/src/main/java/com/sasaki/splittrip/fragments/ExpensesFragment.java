// Localização: com.sasaki.splittrip.fragments
package com.sasaki.splittrip.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sasaki.splittrip.R;
import com.sasaki.splittrip.adapters.ExpenseAdapter;
import com.sasaki.splittrip.models.Expense;
import com.sasaki.splittrip.models.Group;

import java.io.Serializable;

public class ExpensesFragment extends Fragment implements ExpenseAdapter.OnExpenseClickListener {

    private static final String ARG_GROUP = "group";
    private Group group;
    private ExpenseAdapter adapter;

    public static ExpensesFragment newInstance(Group group) {
        ExpensesFragment fragment = new ExpensesFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_GROUP, group);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            group = (Group) getArguments().getSerializable(ARG_GROUP);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Usa o layout simples do Recycler View (activity_main) e esconde elementos não usados
        View view = inflater.inflate(R.layout.activity_main, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_groups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Esconde elementos não utilizados no fragmento
        view.findViewById(R.id.text_greeting).setVisibility(View.GONE);
        view.findViewById(R.id.fab_add_group).setVisibility(View.GONE);

        if (group != null) {
            // Passa a lista de despesas, membros (para saber quem pagou) e o listener
            adapter = new ExpenseAdapter(group.getExpenses(), group.getMembers(), this);
            recyclerView.setAdapter(adapter);
        }

        return view;
    }

    /**
     * Método público para atualizar a lista, chamado pela GroupActivity.
     */
    public void notifyDataSetChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onExpenseClick(Expense expense) {
        // TODO: Implementar visualização/edição de despesa (Diálogo ou nova Activity)
        // Toast.makeText(getContext(), "Clicou em: " + expense.getDescription(), Toast.LENGTH_SHORT).show();
    }
}