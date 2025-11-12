package com.example.splittrip;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.example.splittrip.adapter.GrupoAdapter;
import com.example.splittrip.model.GrupoViagem;
import com.example.splittrip.model.Participante;
import com.example.splittrip.service.CurrencyApiCallback;
import com.example.splittrip.service.CurrencyApiService;
import com.example.splittrip.util.RatesSingleton;

public class MainActivity extends AppCompatActivity implements OnGroupActionListener {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "TripPrefs";
    private static final String KEY_GROUPS = "TripGroups";

    private ArrayList<GrupoViagem> grupos;
    private GrupoAdapter adapter;
    private CurrencyApiService currencyApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currencyApiService = new CurrencyApiService(this);
        loadGroups();
        setupRecyclerView();
        setupFab();

        // Inicia a busca pelas taxas de câmbio (primeira chamada)
        fetchRates();
    }

    private void setupRecyclerView() {
        RecyclerView rvGroups = findViewById(R.id.rvGroups);
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GrupoAdapter(grupos, this);
        rvGroups.setAdapter(adapter);
    }

    private void setupFab() {
        FloatingActionButton fabAddGroup = findViewById(R.id.fabAddGroup);
        fabAddGroup.setOnClickListener(v -> showCreateGroupDialog(null, -1));
    }

    //GESTÃO DE DADOS (Persistência Simples com Gson e SharedPreferences)
    private void loadGroups() {
        String json = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_GROUPS, "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<GrupoViagem>>() {}.getType();
        grupos = gson.fromJson(json, type);
        if (grupos == null) {
            grupos = new ArrayList<>();
        }
    }

    private void saveGroups() {
        Gson gson = new Gson();
        String json = gson.toJson(grupos);
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_GROUPS, json)
                .apply();
    }


    //API E CÂMBIO

    private void fetchRates() {
        currencyApiService.fetchExchangeRates(new CurrencyApiCallback() {
            @Override
            public void onSuccess(Map<String, Double> rates) {
                RatesSingleton.getInstance().updateRates(rates);
                //Toast de sucesso
                Toast.makeText(MainActivity.this, R.string.toast_rates_updated, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                //Toast de erro de conexão
                Toast.makeText(MainActivity.this, R.string.toast_error_connection, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro ao buscar taxas: " + message);
            }
        });
    }

    //DIÁLOGO DE CRIAÇÃO/EDIÇÃO DE GRUPO

    private void showCreateGroupDialog(GrupoViagem existingGroup, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_group, null);
        builder.setView(dialogView);

        //Elementos android.widget
        final EditText etGroupName = dialogView.findViewById(R.id.etGroupName);
        final EditText etDistanceKm = dialogView.findViewById(R.id.etDistanceKm);
        final EditText etFuelPrice = dialogView.findViewById(R.id.etFuelPrice);
        final EditText etTollCost = dialogView.findViewById(R.id.etTollCost);
        final EditText etMembers = dialogView.findViewById(R.id.etMembers);
        final Button btnSave = dialogView.findViewById(R.id.btnSave);
        final Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        //Se estiver editando, pré-preenche os campos
        if (existingGroup != null) {
            etGroupName.setText(existingGroup.getNome());
            etDistanceKm.setText(String.valueOf(existingGroup.getDistanciaKm()));
            etFuelPrice.setText(String.valueOf(existingGroup.getPrecoMedioCombustivel()));
            etTollCost.setText(String.valueOf(existingGroup.getPedagio()));

            //Converte a lista de participantes para uma string separada por quebra de linha
            StringBuilder membersText = new StringBuilder();
            for (Participante p : existingGroup.getParticipantes()) {
                membersText.append(p.getNome()).append("\n");
            }
            etMembers.setText(membersText.toString().trim());
        }

        btnSave.setOnClickListener(v -> {
            //ESTRUTURA TRY/CATCH PARA CONVERSÃO DE STRING PARA DOUBLE
            try {
                String name = etGroupName.getText().toString().trim();
                double distance = Double.parseDouble(etDistanceKm.getText().toString());
                double fuelPrice = Double.parseDouble(etFuelPrice.getText().toString());
                double tollCost = Double.parseDouble(etTollCost.getText().toString());

                if (name.isEmpty()) {
                    Toast.makeText(this, "O nome do grupo não pode ser vazio.", Toast.LENGTH_SHORT).show();
                    return;
                }

                GrupoViagem newOrUpdatedGroup;

                if (existingGroup == null) {
                    //Cria novo grupo
                    newOrUpdatedGroup = new GrupoViagem(name, distance, fuelPrice, tollCost);
                    grupos.add(newOrUpdatedGroup);
                } else {
                    //Atualiza grupo existente
                    newOrUpdatedGroup = existingGroup;
                    newOrUpdatedGroup.setNome(name);
                    newOrUpdatedGroup.setDistanciaKm(distance);
                    newOrUpdatedGroup.setPrecoMedioCombustivel(fuelPrice);
                    newOrUpdatedGroup.setPedagio(tollCost);
                    //Não precisa atualizar a posição na lista, apenas o objeto.
                }

                //Atualiza a lista de participantes
                updateParticipants(newOrUpdatedGroup, etMembers.getText().toString());

                saveGroups();
                adapter.notifyDataSetChanged();
                Toast.makeText(this, R.string.toast_group_saved, Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            } catch (NumberFormatException e) {
                //Toast de erro de entrada
                Toast.makeText(this, R.string.toast_input_error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro de conversão de input: " + e.getMessage());
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateParticipants(GrupoViagem group, String membersText) {
        group.getParticipantes().clear();
        String[] names = membersText.split("\n");
        for (String name : names) {
            String trimmedName = name.trim();
            if (!trimmedName.isEmpty()) {
                group.adicionarParticipante(new Participante(trimmedName));
            }
        }
    }

    // MENU E AÇÕES
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_update_rates) {
            //Opção "Atualizar Taxas de Câmbio"
            fetchRates();
            return true;
        } else if (id == R.id.action_save_trip) {
            //Opção "Salvar Viagem"
            saveGroups();
            Toast.makeText(this, "Grupos salvos localmente.", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Abre a CostsActivity para gerenciar e calcular
    @Override
    public void onGroupClick(GrupoViagem grupo, int position) {
        Intent intent = new Intent(this, CostsActivity.class);
        //Passa o índice e o objeto para CostsActivity
        intent.putExtra("GROUP_INDEX", position);
        intent.putExtra("GROUP_DATA", grupo);
        startActivity(intent);
    }

    //Abre o diálogo de edição
    @Override
    public void onGroupEdit(GrupoViagem grupo, int position) {
        showCreateGroupDialog(grupo, position);
    }

    //Ação de exclusão
    public void deleteGroup(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Grupo")
                .setMessage("Tem certeza que deseja excluir o grupo " + grupos.get(position).getNome() + "?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    grupos.remove(position);
                    saveGroups();
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, R.string.toast_group_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    //Recarrega a lista ao retornar à MainActivity, caso CostsActivity tenha alterado custos
    @Override
    protected void onResume() {
        super.onResume();
        loadGroups(); //Garante que as mudanças de custos extras ou participantes sejam refletidas
        adapter.setGroups(grupos);
        adapter.notifyDataSetChanged();
    }
}