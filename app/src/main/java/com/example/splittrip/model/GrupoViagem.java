package com.example.splittrip.model;

import java.io.Serializable;
import java.util.ArrayList;

public class GrupoViagem implements Serializable {
    private String nome;
    private double distanciaKm;
    private double precoMedioCombustivel;
    private double pedagio;
    private ArrayList<Participante> participantes;
    private ArrayList<CustoExtra> custosExtras;

    //Construtor
    public GrupoViagem(String nome, double distanciaKm, double precoMedioCombustivel, double pedagio) {
        this.nome = nome;
        this.distanciaKm = distanciaKm;
        this.precoMedioCombustivel = precoMedioCombustivel;
        this.pedagio = pedagio;
        this.participantes = new ArrayList<>();
        this.custosExtras = new ArrayList<>();
    }

    //Getters e Setters

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(double distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    public double getPrecoMedioCombustivel() {
        return precoMedioCombustivel;
    }

    public void setPrecoMedioCombustivel(double precoMedioCombustivel) {
        this.precoMedioCombustivel = precoMedioCombustivel;
    }

    public double getPedagio() {
        return pedagio;
    }

    public void setPedagio(double pedagio) {
        this.pedagio = pedagio;
    }

    public ArrayList<Participante> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(ArrayList<Participante> participantes) {
        this.participantes = participantes;
    }

    public ArrayList<CustoExtra> getCustosExtras() {
        return custosExtras;
    }

    public void setCustosExtras(ArrayList<CustoExtra> custosExtras) {
        this.custosExtras = custosExtras;
    }

    //Métodos Auxiliares
    public void adicionarParticipante(Participante participante) {
        this.participantes.add(participante);
    }

    public void adicionarCustoExtra(CustoExtra custo) {
        this.custosExtras.add(custo);
    }

    public int getNumeroParticipantes() {
        return participantes.size();
    }
}