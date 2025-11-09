// Localização: com.sasaki.splittrip.activities
package com.sasaki.splittrip.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sasaki.splittrip.R;
import com.sasaki.splittrip.adapters.GroupAdapter;
import com.sasaki.splittrip.models.Group;
import com.sasaki.splittrip.models.Member;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Activity principal que exibe a lista de grupos do usuário.
 */
public class MainActivity extends AppCompatActivity implements GroupAdapter.GroupClickListener {

    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "user_data";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_USER_NAME = "USER_NAME";
    private static final String KEY_USER_EMAIL = "USER_EMAIL";
    private static final String KEY_GROUPS_JSON = "USER_GROUPS";

    // CHAVES DE RESULTADO (Usadas para comunicação com GroupActivity)
    public static final String EXTRA_NEW_GROUP = "NEW_OR_UPDATED_GROUP";
    public static final String EXTRA_IS_GROUP_DELETED = "IS_GROUP_DELETED";
    public static final String EXTRA_DELETED_GROUP_ID = "DELETED_GROUP_ID";


    private TextView textGreeting;
    private RecyclerView recyclerViewGroups;
    private ExtendedFloatingActionButton fabAddGroup;

    private List<Group> userGroups;
    private GroupAdapter groupAdapter;
    private SharedPreferences userPrefs;
    private Gson gson = new Gson();

    private String currentUserId;
    private String currentUserName;
    private String currentUserEmail;

    // NOVO: O Launcher para receber resultados da GroupActivity
    private ActivityResultLauncher<Intent> groupActivityLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inicialização de UI
        textGreeting = findViewById(R.id.text_greeting);
        recyclerViewGroups = findViewById(R.id.recycler_view_groups);
        fabAddGroup = findViewById(R.id.fab_add_group);

        // 2. Inicialização de dados e usuário
        userPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        checkFirstRunAndSetupUser();
        loadGroups();

        // 3. Configuração do RecyclerView
        setupRecyclerView();

        // 4. Configuração do Launcher para resultados
        setupGroupActivityLauncher();

        // 5. Listener FAB para novo grupo
        fabAddGroup.setOnClickListener(v -> showCreateGroupDialog());

        // 6. Atualização inicial da saudação
        updateGreeting();
    }

    // NOVO: Método para configurar o launcher (CORREÇÃO DO BUG)
    private void setupGroupActivityLauncher() {
        groupActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    boolean shouldSave = false;
                    // Verifica se a Activity retornou com sucesso (RESULT_OK)
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();

                        // 1. Lógica de EXCLUSÃO
                        if (data.getBooleanExtra(EXTRA_IS_GROUP_DELETED, false)) {
                            // Se foi excluído, remove o grupo localmente
                            String deletedGroupId = data.getStringExtra(EXTRA_DELETED_GROUP_ID);

                            if (deletedGroupId != null) {
                                Group groupToRemove = null;
                                int position = -1;
                                for (int i = 0; i < userGroups.size(); i++) {
                                    if (userGroups.get(i).getGroupId().equals(deletedGroupId)) {
                                        groupToRemove = userGroups.get(i);
                                        position = i;
                                        break;
                                    }
                                }

                                if (groupToRemove != null) {
                                    userGroups.remove(position);
                                    groupAdapter.notifyItemRemoved(position);
                                    shouldSave = true; // Grupo removido, precisa salvar
                                }
                            }
                        } else {
                            // 2. Lógica de CRIAÇÃO ou EDIÇÃO
                            // A GroupActivity retorna o grupo atualizado/criado
                            Group updatedGroup = (Group) data.getSerializableExtra(EXTRA_NEW_GROUP);

                            if (updatedGroup != null) {
                                int position = -1;
                                for (int i = 0; i < userGroups.size(); i++) {
                                    if (userGroups.get(i).getGroupId().equals(updatedGroup.getGroupId())) {
                                        position = i; // Encontrado (Edição)
                                        break;
                                    }
                                }

                                if (position == -1) {
                                    // 🚨 CORREÇÃO: Grupo novo (vindo da GroupActivity recém-criada)
                                    // Adiciona o grupo à lista
                                    userGroups.add(0, updatedGroup); // Adiciona no início da lista
                                    groupAdapter.notifyItemInserted(0);
                                    Toast.makeText(this, "Grupo '" + updatedGroup.getGroupName() + "' criado com sucesso!", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Grupo existente atualizado (Edição)
                                    userGroups.set(position, updatedGroup);
                                    groupAdapter.notifyItemChanged(position);
                                }
                                shouldSave = true;
                            }
                        }
                    }

                    // Salva os grupos para persistência se houve qualquer alteração
                    if (shouldSave) {
                        saveGroups();
                    }
                }
        );
    }


    private void setupRecyclerView() {
        userGroups = new ArrayList<>();
        groupAdapter = new GroupAdapter(userGroups, this);
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewGroups.setAdapter(groupAdapter);
    }

    private void updateGreeting() {
        textGreeting.setText("Olá, " + currentUserName + "! Suas Viagens:");
    }

    /**
     * Exibe o diálogo para criar um novo grupo.
     */
    private void showCreateGroupDialog() {
        // Cria um layout para o campo de entrada do nome do grupo
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("Nome da Viagem (Ex: Férias na Praia)");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 20, 50, 0); // Adiciona padding
        container.addView(input);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Criar Novo Grupo")
                .setView(container)
                .setPositiveButton("Criar", (dialog, which) -> {
                    String groupName = input.getText().toString().trim();
                    if (!groupName.isEmpty()) {
                        // 1. Cria o objeto Group
                        String newGroupId = UUID.randomUUID().toString();
                        // O ownerId é o usuário atual
                        Member currentUser = new Member(currentUserId, currentUserName, currentUserEmail);
                        Group newGroup = new Group(newGroupId, groupName, currentUserId, currentUserName, currentUserEmail);
                        newGroup.getMembers().add(currentUser); // Adiciona o criador como primeiro membro

                        // 2. Inicia a Activity do Grupo para VIEWING, esperando um resultado
                        Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                        // 🚨 CORREÇÃO: Passa o grupo *apenas* via Intent. A GroupActivity fará a view inicial.
                        // A adição à lista e o salvamento acontecerão no callback do launcher.
                        intent.putExtra("CURRENT_GROUP", newGroup);

                        // 🚨 CORREÇÃO: Usa o novo launcher para iniciar a Activity
                        groupActivityLauncher.launch(intent);

                    } else {
                        Toast.makeText(this, "O nome do grupo não pode ser vazio.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onGroupClick(Group group, int position) {
        // Inicia a Activity do Grupo para VISUALIZAÇÃO, esperando resultado para DELETE ou UPDATE
        Intent intent = new Intent(MainActivity.this, GroupActivity.class);
        intent.putExtra("CURRENT_GROUP", group);
        // 🚨 CORREÇÃO: Usa o launcher para permitir que a GroupActivity sinalize exclusão/edição.
        groupActivityLauncher.launch(intent);
    }

    // --- Lógica de Persistência e Usuário ---

    private void checkFirstRunAndSetupUser() {
        currentUserId = userPrefs.getString(KEY_USER_ID, null);
        currentUserName = userPrefs.getString(KEY_USER_NAME, "Usuário");
        currentUserEmail = userPrefs.getString(KEY_USER_EMAIL, "email@app.com");

        if (currentUserId == null) {
            setupNewUser();
        }
    }

    private void setupNewUser() {
        // ... (código existente de setupNewUser)
        final EditText nameInput = new EditText(this);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        nameInput.setHint("Seu Nome");

        final EditText emailInput = new EditText(this);
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setHint("Seu Email");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 20, 50, 0);
        container.addView(nameInput);
        container.addView(emailInput);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Configurar Usuário")
                .setView(container)
                .setCancelable(false)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String email = emailInput.getText().toString().trim();

                    if (!name.isEmpty() && !email.isEmpty()) {
                        // Novo ID e atualização das variáveis
                        currentUserId = UUID.randomUUID().toString();
                        currentUserName = name;
                        currentUserEmail = email;

                        // Salva no SharedPreferences
                        userPrefs.edit()
                                .putString(KEY_USER_ID, currentUserId)
                                .putString(KEY_USER_NAME, currentUserName)
                                .putString(KEY_USER_EMAIL, currentUserEmail)
                                .apply();

                        updateGreeting();
                        Toast.makeText(this, "Usuário configurado!", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(this, "Nome e Email são obrigatórios. Tentando novamente.", Toast.LENGTH_LONG).show();
                        setupNewUser(); // Tenta novamente
                    }
                })
                .show();
        // ... (fim do código existente)
    }

    private void saveGroups() {
        String json = gson.toJson(userGroups);
        userPrefs.edit().putString(KEY_GROUPS_JSON, json).apply();
        Log.d(TAG, "Grupos salvos: " + userGroups.size());
    }

    private void loadGroups() {
        String json = userPrefs.getString(KEY_GROUPS_JSON, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Group>>() {}.getType();
            List<Group> loadedGroups = gson.fromJson(json, type);
            if (loadedGroups != null) {
                userGroups.clear();
                userGroups.addAll(loadedGroups);
                if (groupAdapter != null) {
                    groupAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    // --- Menu e Logout ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Limpa o cache (incluindo o ID do usuário e a lista de grupos)
            userPrefs.edit().clear().apply();
            userGroups.clear();
            groupAdapter.notifyDataSetChanged();
            checkFirstRunAndSetupUser();
            Toast.makeText(this, "Logout realizado. Configure um novo usuário.", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}