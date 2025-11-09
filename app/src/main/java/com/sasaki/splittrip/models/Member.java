// Localização: com.sasaki.splittrip.models
package com.sasaki.splittrip.models;

import java.io.Serializable;

/**
 * Representa um membro de um grupo de viagem.
 */
public class Member implements Serializable {

    private String memberId;
    private String name;
    private String email;
    private double balance; // Saldo do membro em relação ao grupo (Valor Pago - Valor Devido)

    public Member(String memberId, String name, String email) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.balance = 0.0;
    }

    // --- Getters ---
    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public double getBalance() { return balance; }

    // --- Setters e Métodos ---
    public void setBalance(double balance) { this.balance = balance; }

    /**
     * Retorna o saldo formatado como string, sem sinal (o sinal é tratado na Activity/Adapter).
     */
    public String getFormattedBalance() {
        return String.format("R$ %.2f", Math.abs(balance));
    }
}