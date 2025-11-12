package com.example.splittrip.service;

import java.util.Map;
 //Interface para comunicação assíncrona.
 //Retorna um mapa com as taxas de câmbio (Moeda -> Taxa BRL) em caso de sucesso
public interface CurrencyApiCallback {
    void onSuccess(Map<String, Double> rates);
    void onError(String message);
}
