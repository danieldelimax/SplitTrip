package com.example.splittrip.model;

import java.io.Serializable;

public class Participante implements Serializable {
    private String nome;

    public Participante(String nome) {
        this.nome = nome;
    }

    //Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}