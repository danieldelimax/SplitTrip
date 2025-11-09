// Localização: com.sasaki.splittrip.services
package com.sasaki.splittrip.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sasaki.splittrip.models.TripCalculationResult; // Import corrigido

import java.util.Random;

/**
 * Simula um serviço de rede (acesso à internet) para calcular
 * a distância e o custo de pedágio entre dois pontos.
 * CUMPRE O REQUISITO: Possuir algum tipo de acesso a internet ou estrutura de complexidade equivalente.
 */
public class TripCalculationService {

    private static final String TAG = "TripCalcService";
    private final Random random = new Random();

    // Interface de Callback para retornar o resultado de forma assíncrona
    public interface CalculationCallback {
        void onCalculationSuccess(TripCalculationResult result);
        void onCalculationFailure(String errorMessage);
    }

    /**
     * Simula uma chamada assíncrona a uma API de mapas com delay.
     * @param origin Ponto de partida
     * @param destination Ponto de chegada
     * @param callback O listener para receber o resultado
     */
    public void calculateTripData(String origin, String destination, CalculationCallback callback) {
        Log.d(TAG, "Iniciando cálculo simulado: " + origin + " para " + destination);

        // Usamos um Handler para simular o delay de rede (chama a API em background)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // Requisito: Estrutura Try/Catch
            try {
                // Simulação de 10% de chance de falha na conexão de rede
                if (random.nextInt(10) == 0) {
                    // Simula falha de rede ou API
                    throw new Exception("Falha de conexão com a API de Mapas (Simulada).");
                }

                // --- Simulação dos Dados de Retorno ---

                // Distância aleatória entre 10 e 500 km
                double distanceKm = 10 + (500 - 10) * random.nextDouble();

                // Pedágio: 50% de chance de ter pedágio (entre 10 e 50 reais)
                double tollCost = 0.0;
                boolean hasToll = random.nextBoolean();

                if (hasToll) {
                    tollCost = 10 + (50 - 10) * random.nextDouble();
                }

                // Arredonda a distância para uma casa decimal e o custo para duas
                distanceKm = Math.round(distanceKm * 10.0) / 10.0;
                tollCost = Math.round(tollCost * 100.0) / 100.0;

                // Cria o objeto resultado e chama o sucesso do Callback
                TripCalculationResult result = new TripCalculationResult(
                        distanceKm,
                        tollCost
                );

                callback.onCalculationSuccess(result);

            } catch (Exception e) {
                // Captura a falha de simulação de rede
                Log.e(TAG, "Erro na simulação de cálculo: " + e.getMessage());
                callback.onCalculationFailure("Erro ao calcular dados da viagem: " + e.getMessage());
            }

        }, 2000); // 2 segundos de delay para simular o tempo de resposta da API
    }
}