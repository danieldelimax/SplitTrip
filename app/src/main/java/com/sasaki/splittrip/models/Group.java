// Localização: com.sasaki.splittrip.models
package com.sasaki.splittrip.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um grupo de viagem com seus membros e despesas.
 * Estrutura principal de dados.
 */
public class Group implements Serializable {

    private final String groupId;
    private String groupName;
    private final String adminMemberId;

    private final List<Expense> expenses;
    private final List<Member> members;

    /**
     * Construtor do Grupo.
     */
    public Group(String groupId, String groupName, String adminMemberId, String adminName, String adminEmail) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.adminMemberId = adminMemberId;
        this.expenses = new ArrayList<>();
        this.members = new ArrayList<>();

        // O administrador é sempre o primeiro membro
        this.members.add(new Member(adminMemberId, adminName, adminEmail));
    }

    /**
     * Calcula o custo total de todas as despesas do grupo.
     */
    public double calculateTotalExpenses() {
        double total = 0;
        for (Expense expense : expenses) {
            if (expense.isTripExpense()) {
                total += expense.calculateTotalTripCost();
            } else {
                total += expense.getAmount();
            }
        }
        return total;
    }

    /**
     * Busca um membro pelo ID.
     */
    public Member getMemberById(String memberId) {
        for (Member member : members) {
            if (member.getMemberId().equals(memberId)) {
                return member;
            }
        }
        return null;
    }

    // --- Getters e Setters ---
    public String getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public String getAdminMemberId() { return adminMemberId; }
    public List<Expense> getExpenses() { return expenses; }
    public List<Member> getMembers() { return members; }

    public void setGroupName(String groupName) { this.groupName = groupName; }
}