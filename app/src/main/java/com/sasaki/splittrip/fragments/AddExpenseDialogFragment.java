package com.sasaki.splittrip.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.sasaki.splittrip.models.Expense;
import com.sasaki.splittrip.ExpenseCreationListener;
import com.sasaki.splittrip.R; // Assumindo que R é o pacote padrão do projeto
import com.sasaki.splittrip.services.TripCalculationService;
import com.sasaki.splittrip.models.TripCalculationResult;

import java.util.Collections;
import java.util.UUID;

/**
 * Fragmento de Diálogo para adicionar uma nova despesa, incluindo a lógica de cálculo de viagem.
 */
public class AddExpenseDialogFragment extends DialogFragment {

    private static final String TAG = "AddExpenseDialog";
    private static final String ARG_USER_ID = "currentUserId";

    // UI Elements
    private TextInputEditText inputDescription;
    private TextInputEditText inputAmount;
    private MaterialCheckBox checkboxIsTripExpense;
    private LinearLayout layoutTripFields;
    private TextInputEditText inputOrigin;
    private TextInputEditText inputDestination;
    private TextInputEditText inputTollCost;
    private TextInputEditText inputFuelCostPerKm;

    private ExpenseCreationListener listener;
    private TripCalculationService tripCalculationService;
    private String currentUserId; // Armazenado como campo para o construtor

    public AddExpenseDialogFragment() {
        // Construtor vazio obrigatório para DialogFragment
    }

    public static AddExpenseDialogFragment newInstance(String currentUserId) {
        AddExpenseDialogFragment fragment = new AddExpenseDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, currentUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializa o serviço de cálculo (simulação de acesso à API)
        tripCalculationService = new TripCalculationService();
        if (getArguments() != null) {
            currentUserId = getArguments().getString(ARG_USER_ID);
        }
        // Aplica o estilo de diálogo em tela cheia se preferir
        // setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_SplitTrip_FullScreenDialog);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Garante que a Activity que chamou implementa o listener
        if (context instanceof ExpenseCreationListener) {
            listener = (ExpenseCreationListener) context;
        } else {
            throw new RuntimeException(context + " must implement ExpenseCreationListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infla o layout: dialog_add_expense.xml
        View view = inflater.inflate(R.layout.dialog_add_expense, container, false);
        setupViews(view);
        return view;
    }

    private void setupViews(View view) {
        // Mapeamento dos componentes de UI
        inputDescription = view.findViewById(R.id.input_description);
        inputAmount = view.findViewById(R.id.input_amount);
        checkboxIsTripExpense = view.findViewById(R.id.checkbox_is_trip_expense);
        layoutTripFields = view.findViewById(R.id.layout_trip_fields);
        inputOrigin = view.findViewById(R.id.input_origin);
        inputDestination = view.findViewById(R.id.input_destination);
        inputTollCost = view.findViewById(R.id.input_toll_cost);
        inputFuelCostPerKm = view.findViewById(R.id.input_fuel_cost_per_km);

        // Inicialmente esconde os campos de viagem
        layoutTripFields.setVisibility(View.GONE);

        // Listener para alternar a visibilidade dos campos de viagem
        checkboxIsTripExpense.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutTripFields.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            // Zera o valor se for uma despesa de viagem (o custo real será calculado)
            if (isChecked) {
                inputAmount.setText("0.00");
                inputAmount.setEnabled(false);
            } else {
                inputAmount.setEnabled(true);
            }
        });

        // Botão de Salvar
        view.findViewById(R.id.button_save_expense).setOnClickListener(v -> saveExpense());
        // Botão de Cancelar
        view.findViewById(R.id.button_cancel_expense).setOnClickListener(v -> dismiss());
    }

    /**
     * Tenta salvar a despesa, fazendo uma chamada assíncrona se for despesa de viagem.
     */
    private void saveExpense() {
        // --- 1. Coleta e Validação dos Dados Comuns ---
        final String description = inputDescription.getText().toString().trim();

        // Converte o valor, tratando strings vazias ou nulas como 0.0
        double amountValue;
        try {
            String amountStr = inputAmount.getText().toString().replace(",", ".");
            amountValue = TextUtils.isEmpty(amountStr) ? 0.0 : Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Valor de gasto inválido.", Toast.LENGTH_LONG).show();
            return;
        }
        final double amount = amountValue;

        final boolean isTripExpense = checkboxIsTripExpense.isChecked();

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(getContext(), "A descrição do gasto é obrigatória.", Toast.LENGTH_LONG).show();
            return;
        }

        if (currentUserId == null) {
            Toast.makeText(getContext(), "Erro: ID de usuário não encontrado.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "currentUserId is null!");
            return;
        }

        // --- 2. Lógica para Despesa Normal (Síncrona) ---
        if (!isTripExpense) {
            if (amount <= 0) {
                Toast.makeText(getContext(), "O valor do gasto deve ser maior que zero.", Toast.LENGTH_LONG).show();
                return;
            }

            Expense newExpense = new Expense(UUID.randomUUID().toString(), description, amount, currentUserId,
                    Collections.singletonList(currentUserId), "Gasto Comum",
                    false, 0, 0); // Sem dados de viagem

            listener.onExpenseCreated(newExpense);
            dismiss();
            return;
        }

        // --- 3. Lógica para Despesa de Viagem (Assíncrona) ---
        // As variáveis PRECISAM ser 'final' aqui para serem acessíveis na classe anônima (CalculationCallback)
        final String origin = inputOrigin.getText().toString().trim();
        final String destination = inputDestination.getText().toString().trim();

        // Coleta os custos de pedágio e combustível por KM, tratando possíveis erros de formato
        double tollCostValue, fuelCostPerKmValue;
        try {
            String tollStr = inputTollCost.getText().toString().replace(",", ".");
            tollCostValue = TextUtils.isEmpty(tollStr) ? 0.0 : Double.parseDouble(tollStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Custo de Pedágio inválido.", Toast.LENGTH_LONG).show();
            return;
        }
        final double tollCost = tollCostValue;

        try {
            String fuelStr = inputFuelCostPerKm.getText().toString().replace(",", ".");
            fuelCostPerKmValue = TextUtils.isEmpty(fuelStr) ? 0.0 : Double.parseDouble(fuelStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Custo/Km de Combustível inválido.", Toast.LENGTH_LONG).show();
            return;
        }
        final double fuelCostPerKm = fuelCostPerKmValue;

        // Validações específicas para viagem
        if (TextUtils.isEmpty(origin) || TextUtils.isEmpty(destination)) {
            Toast.makeText(getContext(), "Preencha a Origem e o Destino para calcular a viagem.", Toast.LENGTH_LONG).show();
            return;
        }

        if (fuelCostPerKm <= 0) {
            Toast.makeText(getContext(), "O Custo/Km do combustível deve ser maior que zero.", Toast.LENGTH_LONG).show();
            return;
        }


        Toast.makeText(getContext(), "Calculando distância via API simulada...", Toast.LENGTH_LONG).show();

        // Chamada de Rede Simulada (Requisito: Acesso à internet/estrutura assíncrona)
        tripCalculationService.calculateTripData(origin, destination, new TripCalculationService.CalculationCallback() {
            @Override
            public void onCalculationSuccess(TripCalculationResult result) {
                // Executado na thread principal após o delay de 2s

                // Cria o objeto Expense usando os dados de viagem calculados
                // O valor 'amount' é 0 porque o custo total será calculado usando (distanceKm * fuelCostPerKm) + tollCost
                Expense newExpense = new Expense(UUID.randomUUID().toString(), description, 0, currentUserId,
                        Collections.singletonList(currentUserId), "Transporte",
                        true, result.getDistanceKm(), result.getTollCost());

                // Agora newExpense.setFuelCostPerKm(fuelCostPerKm) usa a variável final 'fuelCostPerKm'
                // que foi lida antes da chamada assíncrona.
                newExpense.setFuelCostPerKm(fuelCostPerKm); // Adiciona o custo por Km do usuário

                // Exibe o resultado da simulação
                Toast.makeText(getContext(),
                        "Viagem calculada: " + result.getDistanceKm() + "km. Despesa salva!",
                        Toast.LENGTH_LONG).show();

                listener.onExpenseCreated(newExpense);
                dismiss();
            }

            @Override
            public void onCalculationFailure(String errorMessage) {
                // Requisito: Try/Catch no serviço de rede (propagado como falha aqui)
                Toast.makeText(getContext(), "Falha ao salvar despesa: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}