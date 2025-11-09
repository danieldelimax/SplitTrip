// Localização: com.sasaki.splittrip.services
package com.sasaki.splittrip.services;

import com.sasaki.splittrip.models.Expense;
import com.sasaki.splittrip.models.Group;
import com.sasaki.splittrip.models.Member;

import java.util.HashMap;
import java.util.Map;

/**
 * Serviço responsável por calcular a divisão igualitária de despesas.
 * A lógica principal é: Total da Despesa / Número de Membros.
 */
public class PaymentCalculatorService {

    /**
     * Calcula a divisão do valor total de todas as despesas entre todos os membros do grupo.
     * @param group O grupo contendo as despesas e a lista de membros.
     * @return Um mapa com o MemberId e o saldo final (Valor Pago - Valor Devido).
     */
    public Map<String, Double> calculateEqualSplitBalances(Group group) {
        // Mapas para rastrear o que cada um PAGOU e o que cada um DEVE (sua parte na divisão)
        Map<String, Double> paidMap = new HashMap<>();
        Map<String, Double> owedMap = new HashMap<>();

        // Inicializa os mapas com todos os membros e saldo zero
        for (Member member : group.getMembers()) {
            paidMap.put(member.getMemberId(), 0.0);
            owedMap.put(member.getMemberId(), 0.0);
        }

        int totalMembers = group.getMembers().size();
        if (totalMembers == 0) {
            return new HashMap<>(); // Não há o que calcular
        }

        // Processa cada despesa
        for (Expense expense : group.getExpenses()) {

            // 1. Determina o valor total do gasto, usando o cálculo de viagem se for o caso
            // CORREÇÃO: Utiliza calculateTotalTripCost() se for despesa de viagem.
            double totalAmount = expense.isTripExpense() ? expense.calculateTotalTripCost() : expense.getAmount();

            if (totalAmount <= 0) continue;

            // 2. Calcula o valor que cada um DEVE (sua parte igual)
            double individualOwedAmount = totalAmount / totalMembers;

            // 3. Atualiza o valor TOTAL pago pelo pagador
            String paidById = expense.getPaidByMemberId();
            paidMap.put(paidById, paidMap.getOrDefault(paidById, 0.0) + totalAmount);

            // 4. Adiciona o valor devido (parte igual) a CADA membro
            for (Member member : group.getMembers()) {
                String memberId = member.getMemberId();
                owedMap.put(memberId, owedMap.getOrDefault(memberId, 0.0) + individualOwedAmount);
            }
        }

        // 5. Calcula o Saldo Final (Pago - Devido) para cada membro
        Map<String, Double> finalBalanceMap = new HashMap<>();
        for (Member member : group.getMembers()) {
            String memberId = member.getMemberId();
            double finalBalance = paidMap.getOrDefault(memberId, 0.0) - owedMap.getOrDefault(memberId, 0.0);

            // Arredonda para duas casas decimais para evitar imprecisões de ponto flutuante
            finalBalance = Math.round(finalBalance * 100.0) / 100.0;

            finalBalanceMap.put(memberId, finalBalance);
        }

        return finalBalanceMap;
    }
}