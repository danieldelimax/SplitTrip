package com.example.splittrip.util;

import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//Singleton responsável por armazenar as taxas de câmbio globalmente.
public class RatesSingleton {
    private static final String TAG = "RatesSingleton";
    private static RatesSingleton instance;
    private Map<String, Double> currentRates;

    private RatesSingleton() {
        //Inicializa o mapa com a moeda base
        currentRates = new HashMap<>();
        currentRates.put("BRL", 1.0);
    }

    public static synchronized RatesSingleton getInstance() {
        if (instance == null) {
            instance = new RatesSingleton();
        }
        return instance;
    }

    //Atualiza as taxas de câmbio com os dados vindos da API
    public void updateRates(Map<String, Double> newRates) {
        if (newRates != null && !newRates.isEmpty()) {
            currentRates.clear();
            currentRates.putAll(newRates);
            Log.i(TAG, "Taxas de câmbio atualizadas. Total de " + currentRates.size() + " moedas.");
        }
    }

    //Obtém as taxas de câmbio atuais
    public Map<String, Double> getRates() {
        return Collections.unmodifiableMap(currentRates);
    }

    //Converte um valor de uma moeda de origem para a moeda base (BRL)
    public double convertToBRL(double valor, String moedaOriginal) {
        String key = moedaOriginal.toUpperCase();
        Double rate = currentRates.get(key);

        if (rate != null && rate > 0) {
            //Valor em BRL = Valor original * Taxa de câmbio
            return valor * rate;
        }

        if (!key.equals("BRL")) {
            Log.w(TAG, "Taxa para " + key + " não encontrada ou inválida. Retornando valor original (assumindo BRL).");
        }

        //Se a taxa não for encontrada ou a moeda for BRL, retorna o valor original.
        return valor;
    }
}