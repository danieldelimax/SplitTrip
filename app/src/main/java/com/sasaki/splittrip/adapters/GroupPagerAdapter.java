// Localização: com.sasaki.splittrip.adapters
package com.sasaki.splittrip.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sasaki.splittrip.fragments.ExpensesFragment;
import com.sasaki.splittrip.fragments.MembersFragment;
import com.sasaki.splittrip.fragments.SettingsFragment;
import com.sasaki.splittrip.models.Group;

/**
 * Adapter para gerenciar os Fragments de Gastos, Membros e Configurações
 * dentro do ViewPager2 da GroupActivity.
 */
public class GroupPagerAdapter extends FragmentStateAdapter {

    private final Group group;

    public GroupPagerAdapter(@NonNull FragmentActivity fragmentActivity, Group group) {
        super(fragmentActivity);
        this.group = group;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Usa os métodos newInstance() refatorados dos Fragments
        switch (position) {
            case 0:
                return ExpensesFragment.newInstance(group);
            case 1:
                return MembersFragment.newInstance(group);
            case 2:
                // Passa apenas os dados necessários
                return SettingsFragment.newInstance(group.getGroupId(), group.getGroupName());
            default:
                // Retorna o primeiro Fragment em caso de erro
                return ExpensesFragment.newInstance(group);
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Gastos, Membros, Configurações
    }

    // Método para permitir a Activity acessar os Fragments (necessário para o Recalculo)
    public Fragment getRegisteredFragment(int position, FragmentActivity activity) {
        // O ViewPager2 não mantém referências fáceis. A melhor prática é buscá-lo pelo tag
        // gerada automaticamente pelo FragmentStateAdapter.
        String tag = "f" + getItemId(position);
        return activity.getSupportFragmentManager().findFragmentByTag(tag);
    }
}