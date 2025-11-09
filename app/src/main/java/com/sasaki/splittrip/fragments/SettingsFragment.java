package com.sasaki.splittrip.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sasaki.splittrip.R;
// A importação é OBRIGATÓRIA porque GroupActivity está em um pacote diferente:
import com.sasaki.splittrip.activities.GroupActivity;

/**
 * Fragmento para a aba de Configurações, responsável por exibir o link de convite
 * e permitir que o usuário o compartilhe.
 */
public class SettingsFragment extends Fragment {

    // Chaves para os argumentos (Bundle)
    private static final String ARG_GROUP_ID = "groupId";
    private static final String ARG_GROUP_NAME = "groupName";

    private String groupId;
    private String groupName;

    public SettingsFragment() {
        // Construtor vazio obrigatório
    }

    /**
     * Método de fábrica para criar uma nova instância de SettingsFragment
     * com os argumentos necessários.
     * @param groupId ID do grupo.
     * @param groupName Nome do grupo.
     * @return Uma nova instância de SettingsFragment.
     */
    public static SettingsFragment newInstance(String groupId, String groupName) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        args.putString(ARG_GROUP_NAME, groupName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
            groupName = getArguments().getString(ARG_GROUP_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout fragment_settings.xml (Necessário criar este arquivo no res/layout)
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        TextView linkTextView = view.findViewById(R.id.text_invite_link);
        Button shareButton = view.findViewById(R.id.button_share_link);
        // 🚨 NOVO: Referência ao botão de apagar
        Button deleteButton = view.findViewById(R.id.button_delete_group);

        // Gera o link de convite e exibe no TextView
        String inviteLink = "splittrip://invite?group=" + groupId;
        linkTextView.setText(inviteLink);

        // Listener para o botão de compartilhamento
        shareButton.setOnClickListener(v -> {
            // Chama o método de compartilhamento implementado na Activity
            if (getActivity() instanceof GroupActivity) {
                ((GroupActivity) getActivity()).shareGroupInviteLink();
            } else {
                Toast.makeText(getContext(), "Erro ao iniciar compartilhamento. A Activity não é GroupActivity.", Toast.LENGTH_SHORT).show();
            }
        });

        // 🚨 NOVO: Listener para o botão de APAGAR GRUPO (PERMANENTE)
        deleteButton.setOnClickListener(v -> {
            // Chama o novo método de exclusão implementado na Activity
            if (getActivity() instanceof GroupActivity) {
                // O método deleteGroup() na GroupActivity exibe o diálogo de confirmação
                ((GroupActivity) getActivity()).deleteGroup();
            } else {
                Toast.makeText(getContext(), "Erro ao excluir. A Activity não é GroupActivity.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}