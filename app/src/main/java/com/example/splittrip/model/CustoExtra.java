package com.example.splittrip.model;

import java.io.Serializable;

public class CustoExtra implements Serializable {
    private String descricao;
    private double valor; //Valor na moeda de origem
    private String moeda; //Ex: "USD", "EUR", "BRL"

    public CustoExtra(String descricao, double valor, String moeda) {
        this.descricao = descricao;
        this.valor = valor;
        this.moeda = moeda;
    }

    //Getters e Setters
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getMoeda() {
        return moeda;
    }

    public void setMoeda(String moeda) {
        this.moeda = moeda;
    }
}