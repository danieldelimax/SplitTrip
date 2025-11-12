package com.example.splittrip;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.example.splittrip.adapter.CustoExtraAdapter;
import com.example.splittrip.model.CustoExtra;
import com.example.splittrip.model.GrupoViagem;
import com.example.splittrip.util.RatesSingleton;

public class CostsActivity extends AppCompatActivity
        implements CustoExtraAdapter.OnCostDeleteListener {

    private static final String TAG = "CostsActivity";
    private static final String PREFS_NAME = "TripPrefs";
    private static final String KEY_GROUPS = "TripGroups";

    private GrupoViagem grupoAtual;
    private int grupoIndex;
    private CustoExtraAdapter adapter;

    //Elementos de UI
    private TextView tvTripTitle;
    private TextView tvTotalTripCostValue;
    private TextView tvCostPerMemberValue;
    private Button btnCalculate;
    private ImageButton btnAddExtraCost;

    //Suporte para cálculos
    private RatesSingleton ratesManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_costs);

        //Inicializa o RatesSingleton
        ratesManager = RatesSingleton.getInstance();

        //Carregar Dados da Intent
        if (getIntent().hasExtra("GROUP_INDEX") && getIntent().hasExtra("GROUP_DATA")) {
            grupoIndex = getIntent().getIntExtra("GROUP_INDEX", -1);
            grupoAtual = (GrupoViagem) getIntent().getSerializableExtra("GROUP_DATA");
        }

        if (grupoAtual == null || grupoIndex == -1) {
            Toast.makeText(this, "Erro: Dados do grupo não encontrados.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //Inicializar Views
        initializeViews();

        //Configurar RecyclerView
        setupRecyclerView();

        //Configurar Título
        tvTripTitle.setText(getString(R.string.title_activity_costs) + " - " + grupoAtual.getNome());

        //Configurar Listeners
        btnAddExtraCost.setOnClickListener(v -> showAddCostDialog());
        btnCalculate.setOnClickListener(v -> calculateTripCost());

        //Calcula o custo inicial
        calculateTripCost();
    }

    private void initializeViews() {
        tvTripTitle = findViewById(R.id.tvTripTitle);
        tvTotalTripCostValue = findViewById(R.id.tvTotalTripCostValue);
        tvCostPerMemberValue = findViewById(R.id.tvCostPerMemberValue);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnAddExtraCost = findViewById(R.id.btnAddExtraCost);
    }

    private void setupRecyclerView() {
        RecyclerView rvExtraCosts = findViewById(R.id.rvExtraCosts);
        rvExtraCosts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustoExtraAdapter(grupoAtual.getCustosExtras(), this);
        rvExtraCosts.setAdapter(adapter);
    }


    // LÓGICA DE CÁLCULO
    private void calculateTripCost() {
        double totalCostBRL = 0.0;
        int numParticipants = grupoAtual.getNumeroParticipantes();

        //Se não houver participantes, não é possível dividir
        if (numParticipants == 0) {
            tvTotalTripCostValue.setText("R$ 0.00");
            tvCostPerMemberValue.setText("N/A (Adicione membros)");
            Toast.makeText(this, "Adicione pelo menos um membro para calcular!", Toast.LENGTH_LONG).show();
            return;
        }

        //Custo fixo (Combustível + Pedágio)

        double litrosConsumidosEstimados = grupoAtual.getDistanciaKm() / 10.0; // Ex: 10 km/L
        double custoCombustivel = litrosConsumidosEstimados * grupoAtual.getPrecoMedioCombustivel();

        totalCostBRL += custoCombustivel;
        totalCostBRL += grupoAtual.getPedagio();

        //Custo Extra (com conversão de moeda)
        for (CustoExtra custo : grupoAtual.getCustosExtras()) {
            double valorConvertido;

            //O RatesSingleton gerencia a conversão de moeda.
            valorConvertido = ratesManager.convertToBRL(custo.getValor(), custo.getMoeda());
            totalCostBRL += valorConvertido;
        }

        //Resultado Final
        double costPerMember = totalCostBRL / numParticipants;

        tvTotalTripCostValue.setText(String.format("R$ %.2f", totalCostBRL));
        tvCostPerMemberValue.setText(String.format("R$ %.2f", costPerMember));

        //Toast de sucesso
        Toast.makeText(this, R.string.toast_calculation_finished, Toast.LENGTH_SHORT).show();

        //Salva o estado do grupo
        saveGroupChangesToMainList();
    }


    // DIÁLOGO DE CUSTO EXTRA

    private void showAddCostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_extra_cost, null);
        builder.setView(dialogView);

        //Elementos de UI
        final EditText etDescription = dialogView.findViewById(R.id.etCostDescription);
        final EditText etValue = dialogView.findViewById(R.id.etCostValue);
        final Spinner spinnerCurrency = dialogView.findViewById(R.id.spinnerCurrency); // Requisito 6: Spinner
        final Button btnAddCost = dialogView.findViewById(R.id.btnAddCost);
        final Button btnCancelCost = dialogView.findViewById(R.id.btnCancelCost);

        //Configura o Spinner de Moedas
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        btnAddCost.setOnClickListener(v -> {
            //ESTRUTURA TRY/CATCH PARA CONVERSÃO DE STRING PARA DOUBLE
            try {
                String description = etDescription.getText().toString().trim();
                double value = Double.parseDouble(etValue.getText().toString());
                String currency = spinnerCurrency.getSelectedItem().toString();

                if (description.isEmpty() || value <= 0) {
                    Toast.makeText(this, "Preencha a descrição e valor corretamente.", Toast.LENGTH_SHORT).show();
                    return;
                }

                CustoExtra novoCusto = new CustoExtra(description, value, currency);
                grupoAtual.adicionarCustoExtra(novoCusto);

                //Notifica o Adapter e recalcula
                this.adapter.notifyItemInserted(grupoAtual.getCustosExtras().size() - 1);
                calculateTripCost();

                Toast.makeText(this, R.string.toast_cost_added, Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            } catch (NumberFormatException e) {
                //Toast de erro de entrada
                Toast.makeText(this, R.string.toast_input_error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro de conversão de input: " + e.getMessage());
            }
        });

        btnCancelCost.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // IMPLEMENTAÇÃO DO OnCostDeleteListener
    @Override
    public void onCostDeleted(int position) {
        if (position >= 0 && position < grupoAtual.getCustosExtras().size()) {
            grupoAtual.getCustosExtras().remove(position);
            adapter.notifyItemRemoved(position);
            calculateTripCost(); // Recalcula após a exclusão
            Toast.makeText(this, "Custo removido.", Toast.LENGTH_SHORT).show();
        }
    }

    // PERSISTÊNCIA (Atualiza a lista principal de grupos no SharedPreferences)
    private void saveGroupChangesToMainList() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(KEY_GROUPS, "[]");
        Gson gson = new Gson();

        Type type = new TypeToken<ArrayList<GrupoViagem>>() {}.getType();
        ArrayList<GrupoViagem> grupos = gson.fromJson(json, type);

        if (grupos != null && grupoIndex >= 0 && grupoIndex < grupos.size()) {
            //Substitui o grupo antigo pelo objeto grupoAtual modificado
            grupos.set(grupoIndex, grupoAtual);

            //Salva a lista completa novamente
            String updatedJson = gson.toJson(grupos);
            prefs.edit().putString(KEY_GROUPS, updatedJson).apply();

            Log.d(TAG, "Alterações do grupo salvas no SharedPreferences.");
        } else {
            Log.e(TAG, "Erro ao salvar: índice do grupo inválido ou lista vazia.");
        }
    }
}