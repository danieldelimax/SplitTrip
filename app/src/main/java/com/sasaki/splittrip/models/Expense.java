// Localização: com.sasaki.splittrip.models
package com.sasaki.splittrip.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Representa uma despesa individual dentro de um grupo, com suporte a cálculos de viagem.
 */
public class Expense implements Serializable {

    private final String expenseId;
    private final String description;
    private final double amount;
    private final String paidByMemberId;
    private final List<String> splitAmongMemberIds;
    private final String categoryTag;
    private final Date date;

    // Complexidade de Viagem
    private final boolean isTripExpense;
    private final double distanceKm;
    private final double tollCost;
    private double fuelCostPerKm; // Pode ser ajustado após a criação

    /**
     * Construtor completo do Expense.
     * @param expenseId ID único.
     * @param description Breve descrição.
     * @param amount Valor da despesa (0 se for despesa de viagem com cálculo pendente).
     * @param paidByMemberId ID do pagador.
     * @param splitAmongMemberIds IDs dos membros que dividirão.
     * @param categoryTag Categoria.
     * @param isTripExpense Flag de despesa de viagem.
     * @param distanceKm Distância (retornada pela API simulada).
     * @param tollCost Custo do pedágio.
     */
    public Expense(String expenseId, String description, double amount, String paidByMemberId,
                   List<String> splitAmongMemberIds, String categoryTag,
                   boolean isTripExpense, double distanceKm, double tollCost) {
        this.expenseId = expenseId;
        this.description = description;
        this.amount = amount;
        this.paidByMemberId = paidByMemberId;
        this.splitAmongMemberIds = splitAmongMemberIds;
        this.categoryTag = categoryTag;
        this.date = new Date();
        this.isTripExpense = isTripExpense;
        this.distanceKm = distanceKm;
        this.tollCost = tollCost;
        this.fuelCostPerKm = 0.50; // Valor padrão para cálculo de combustível
    }

    /**
     * Calcula o custo total da viagem (combustível + pedágio).
     * CORREÇÃO: Usa 'distanceKm' e 'fuelCostPerKm'.
     */
    public double calculateTotalTripCost() {
        // Distância * Custo por Km + Pedágio
        return (distanceKm * fuelCostPerKm) + tollCost;
    }

    // --- Getters ---
    public String getExpenseId() { return expenseId; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getPaidByMemberId() { return paidByMemberId; }
    public List<String> getSplitAmongMemberIds() { return splitAmongMemberIds; }
    public String getCategoryTag() { return categoryTag; }
    public Date getDate() { return date; }
    public boolean isTripExpense() { return isTripExpense; }
    public double getDistanceKm() { return distanceKm; }
    public double getTollCost() { return tollCost; }
    public double getFuelCostPerKm() { return fuelCostPerKm; }

    // --- Setters (apenas para campos que podem ser modificados após a criação) ---
    public void setFuelCostPerKm(double fuelCostPerKm) { this.fuelCostPerKm = fuelCostPerKm; }

    // Método mantido, mas a lógica de split é primariamente tratada no PaymentCalculatorService
    public double calculateSplitAmount() {
        int count = splitAmongMemberIds.size();
        double total = isTripExpense ? calculateTotalTripCost() : amount;
        return count > 0 ? total / count : 0;
    }
}