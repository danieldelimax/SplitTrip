// Localização: com.sasaki.splittrip.activities
package com.sasaki.splittrip.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sasaki.splittrip.ExpenseCreationListener;
import com.sasaki.splittrip.R;
import com.sasaki.splittrip.adapters.GroupPagerAdapter;
import com.sasaki.splittrip.fragments.AddExpenseDialogFragment;
import com.sasaki.splittrip.fragments.ExpensesFragment;
import com.sasaki.splittrip.fragments.MembersFragment;
import com.sasaki.splittrip.models.Expense;
import com.sasaki.splittrip.models.Group;
import com.sasaki.splittrip.models.Member;
import com.sasaki.splittrip.services.PaymentCalculatorService;

import java.util.Map;
import java.util.UUID;

/**
 * Activity principal de visualização de um grupo, utilizando ViewPager2 para
 * navegar entre Gastos, Membros e Configurações.
 */
public class GroupActivity extends AppCompatActivity implements ExpenseCreationListener {

    // CHAVES DE RESULTADO (As mesmas usadas na MainActivity)
    public static final String EXTRA_NEW_GROUP = "NEW_OR_UPDATED_GROUP";
    public static final String EXTRA_IS_GROUP_DELETED = "IS_GROUP_DELETED";
    public static final String EXTRA_DELETED_GROUP_ID = "DELETED_GROUP_ID";

    private TextView groupNameTextView;
    private TextView userBalanceTextView;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private GroupPagerAdapter pagerAdapter;
    private ExtendedFloatingActionButton fabAddExpense;

    private Group currentGroup;
    private final PaymentCalculatorService paymentCalculator = new PaymentCalculatorService();

    // Dados do usuário atual
    private String currentUserId;

    // NOVO: Flag para controlar se a exclusão foi solicitada
    private boolean isGroupDeleted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        // 1. Inicialização de Dados do Usuário
        SharedPreferences userPrefs = getSharedPreferences("user_data", Context.MODE_PRIVATE);
        currentUserId = userPrefs.getString("USER_ID", "UNKNOWN");

        // 2. Mapeamento UI e Configuração do Toolbar
        groupNameTextView = findViewById(R.id.text_group_name);
        userBalanceTextView = findViewById(R.id.text_user_balance);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        fabAddExpense = findViewById(R.id.fab_add_expense);

        // 3. Carregamento do Grupo (Intent normal ou Deep Link)
        if (!handleDeepLinkIntent(getIntent())) {
            currentGroup = (Group) getIntent().getSerializableExtra("CURRENT_GROUP");
        }

        if (currentGroup == null) {
            Toast.makeText(this, "Erro: Grupo não encontrado.", Toast.LENGTH_SHORT).show();
            // Define o resultado como cancelado se não houver grupo
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        // 4. Configuração Inicial
        // Configura o Action Bar para exibir o nome do grupo e o botão de voltar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentGroup.getGroupName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        groupNameTextView.setText(currentGroup.getGroupName());
        setupViewPager();
        calculateAndDisplayBalance();

        // 5. Listener FAB
        fabAddExpense.setOnClickListener(this::addExpenseClicked);
    }

    // Suporte para o botão de voltar da Action Bar
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Chama o finish() corrigido
        return true;
    }

    // 🚨 CORREÇÃO: Sobrescreve o finish() para definir o resultado do Intent.
    @Override
    public void finish() {
        Intent resultIntent = new Intent();

        if (currentGroup != null) {
            if (isGroupDeleted) {
                // Se o grupo foi excluído, sinaliza e passa o ID para a MainActivity
                resultIntent.putExtra(EXTRA_IS_GROUP_DELETED, true);
                resultIntent.putExtra(EXTRA_DELETED_GROUP_ID, currentGroup.getGroupId());
            } else {
                // Se fechou normalmente, retorna o grupo (para MainActivity adicionar/atualizar)
                resultIntent.putExtra(EXTRA_NEW_GROUP, currentGroup);
            }
        }

        setResult(Activity.RESULT_OK, resultIntent);
        super.finish();
    }

    // --- Lógica de ViewPager e Tabs ---

    private void setupViewPager() {
        pagerAdapter = new GroupPagerAdapter(this, currentGroup);
        viewPager.setAdapter(pagerAdapter);

        // Nomes das abas
        String[] tabTitles = {"Gastos", "Membros", "Configurações"};

        // Conecta o TabLayout ao ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();
    }

    // --- Lógica de Cálculo e Atualização ---

    /**
     * Calcula o saldo de todos os membros e atualiza a UI.
     */
    private void calculateAndDisplayBalance() {
        // Usa o serviço para obter os saldos finais
        Map<String, Double> balances = paymentCalculator.calculateEqualSplitBalances(currentGroup);

        // Aplica o saldo a cada objeto Member no grupo
        for (Member member : currentGroup.getMembers()) {
            double finalBalance = balances.getOrDefault(member.getMemberId(), 0.0);
            member.setBalance(finalBalance);
        }

        // Exibe o saldo do usuário atual na CardView
        double userBalance = balances.getOrDefault(currentUserId, 0.0);
        updateUserBalanceUI(userBalance);

        // Notifica os fragments para atualizarem a lista
        notifyFragmentsOfDataChange();
    }

    /**
     * Atualiza o TextView do saldo do usuário com a cor correta.
     */
    private void updateUserBalanceUI(double balance) {
        String balanceText;
        int color;

        if (balance > 0) {
            balanceText = "Você deve receber: " + String.format("R$ %.2f", balance);
            // Saldo positivo usa a cor de destaque (colorAccent)
            color = getColor(R.color.colorAccent);
        } else if (balance < 0) {
            balanceText = "Você deve pagar: " + String.format("R$ %.2f", Math.abs(balance));
            // Saldo negativo (dívida) usa Vermelho para destaque.
            color = getColor(android.R.color.holo_red_dark);
        } else {
            balanceText = "Seu saldo está zerado!";
            // Saldo zero usa a cor preta (black)
            color = getColor(R.color.black);
        }

        userBalanceTextView.setText(balanceText);
        userBalanceTextView.setTextColor(color);
    }

    private void notifyFragmentsOfDataChange() {
        // Tenta obter os fragments registrados pelo PagerAdapter e os notifica
        Fragment expensesFragment = pagerAdapter.getRegisteredFragment(0, this);
        if (expensesFragment instanceof ExpensesFragment) {
            ((ExpensesFragment) expensesFragment).notifyDataSetChanged();
        }

        Fragment membersFragment = pagerAdapter.getRegisteredFragment(1, this);
        if (membersFragment instanceof MembersFragment) {
            ((MembersFragment) membersFragment).notifyDataSetChanged();
        }
        // Configurações não precisa ser notificado
    }

    // --- Lógica de Despesa (FAB e Callback) ---

    public void addExpenseClicked(View view) {
        // Abre o Diálogo para adicionar despesa
        AddExpenseDialogFragment.newInstance(currentUserId)
                .show(getSupportFragmentManager(), "AddExpenseDialog");
    }

    @Override
    public void onExpenseCreated(Expense newExpense) {
        // Implementação do callback do ExpenseCreationListener
        currentGroup.getExpenses().add(newExpense);

        // Recalcula e atualiza toda a UI
        calculateAndDisplayBalance();
    }

    // --- Lógica de Exclusão ---

    /**
     * Chamado pelo SettingsFragment para iniciar o processo de exclusão do grupo.
     * Exibe um diálogo de confirmação antes de excluir.
     */
    public void deleteGroup() {
        if (currentGroup == null) {
            Toast.makeText(this, "Erro: Nenhum grupo carregado para excluir.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Exibe o diálogo de confirmação
        new MaterialAlertDialogBuilder(this)
                .setTitle("Excluir Grupo Permanente")
                .setMessage("Tem certeza de que deseja excluir o grupo '" + currentGroup.getGroupName() + "'?\n\nEsta ação não pode ser desfeita.")
                .setPositiveButton("Sim, Excluir", (dialog, which) -> {
                    // Ação de Exclusão:
                    // 1. Simula a exclusão no banco de dados/cache local.

                    // 2. Notifica o usuário
                    Toast.makeText(this, "Grupo '" + currentGroup.getGroupName() + "' excluído permanentemente.", Toast.LENGTH_LONG).show();

                    // 3. Define a flag de exclusão e finaliza a Activity
                    isGroupDeleted = true;
                    finish(); // Chama o finish() corrigido
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // --- Lógica de Deep Link e Compartilhamento ---

    /**
     * Implementação para ser chamada pelo SettingsFragment e pelo Menu.
     */
    public void shareGroupInviteLink() {
        if (currentGroup == null) return;

        String link = "splittrip://invite?group=" + currentGroup.getGroupId();
        String message = "Ei, junte-se ao meu grupo de viagem '" + currentGroup.getGroupName() + "' no SplitTrip! Clique aqui: " + link;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(shareIntent, "Convidar para o grupo"));
    }

    /**
     * Trata o Deep Link vindo de uma notificação ou outro app.
     */
    private boolean handleDeepLinkIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null && "invite".equals(uri.getHost())) {
                String groupId = uri.getQueryParameter("group");

                // Simulação: se veio de um convite, cria um grupo simulado para teste.
                if (groupId != null) {
                    currentGroup = createSimulatedGroup(groupId, "Grupo de Convite (" + groupId.substring(0, 4) + ")");
                    // Adiciona o usuário atual ao grupo de convite
                    if (currentGroup.getMemberById(currentUserId) == null) {
                        currentGroup.getMembers().add(new Member(currentUserId,
                                getSharedPreferences("user_data", Context.MODE_PRIVATE).getString("USER_NAME", "Você"),
                                getSharedPreferences("user_data", Context.MODE_PRIVATE).getString("USER_EMAIL", "email@app.com")));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Método auxiliar para criar um grupo simulado ao receber um Deep Link.
     */
    private Group createSimulatedGroup(String id, String name) {
        // Cria um grupo básico (o restante dos dados viria de uma API/DB)
        return new Group(id, name, "ADMIN-" + id.substring(0, 0), "Admin Simulado", "admin@simulado.com");
    }

    // --- Menu ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_invite) {
            shareGroupInviteLink();
            return true;
        } else if (id == R.id.action_settle_up) {
            // TODO: Implementar a lógica de "Acertar Contas" (Sugestões de Pagamento)
            Toast.makeText(this, "Sugestões de pagamento em breve!", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}