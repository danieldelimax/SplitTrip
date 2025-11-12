package com.example.splittrip.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CurrencyApiService {

    private static final String TAG = "CurrencyApiService";
    //Moedas a serem buscadas: USD, EUR, GBP (em relação ao BRL)
    private static final String API_URL_BASE = "https://economia.awesomeapi.com.br/json/last/USD-BRL,EUR-BRL,GBP-BRL";
    private static final String API_TOKEN = "560b86abf0431dd2b056032b3aa8575ed0b9c128ab8d6d0e321ecf55a00bf1fb";

    private final RequestQueue requestQueue;

    public CurrencyApiService(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
    }

    //Busca as taxas de câmbio e retorna o resultado via callback.
    public void fetchExchangeRates(final CurrencyApiCallback callback) {
        String url = API_URL_BASE + "?token=" + API_TOKEN;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Map<String, Double> rates = new HashMap<>();
                    rates.put("BRL", 1.0); // Moeda base é sempre 1.0

                    //ESTRUTURA TRY/CATCH PARA PARSING DO JSON
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        //Função auxiliar para extrair a taxa de câmbio (bid)
                        rates.put("USD", extractRate(jsonObject, "USDBRL"));
                        rates.put("EUR", extractRate(jsonObject, "EURBRL"));
                        rates.put("GBP", extractRate(jsonObject, "GBPBRL"));

                        //Notifica sucesso, passando o mapa de taxas
                        callback.onSuccess(rates);

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing Error: " + e.getMessage());
                        callback.onError("Erro ao processar dados de câmbio: formato inválido.");
                    }
                },
                error -> {
                    Log.e(TAG, "Network Error: " + error.toString());

                    //TRATAMENTO DE ERROS DE CONEXÃO
                    String errorMessage;
                    if (error instanceof com.android.volley.NoConnectionError || error.networkResponse == null) {
                        errorMessage = "Erro ao buscar taxa de câmbio! Verifique sua conexão com a Internet.";
                    } else {
                        //Trata outros erros de resposta
                        errorMessage = "Erro na API: Código " + error.networkResponse.statusCode;
                    }

                    //Notifica erro de rede
                    callback.onError(errorMessage);
                }
        );

        requestQueue.add(stringRequest);
    }

    //Método auxiliar interno para extrair a taxa "bid" do JSON
    private double extractRate(JSONObject jsonObject, String key) throws JSONException {
        if (jsonObject.has(key)) {
            return jsonObject.getJSONObject(key).getDouble("bid");
        }
        //Se a chave não existir na API, retorna 1.0 (ou outro valor padrão seguro)
        return 1.0;
    }
}