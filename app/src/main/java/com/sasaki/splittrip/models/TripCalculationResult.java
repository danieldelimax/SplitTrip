// Localização: com.sasaki.splittrip.models
package com.sasaki.splittrip.models;

/**
 * Classe POJO para o resultado retornado pela simulação de API de mapas.
 */
public class TripCalculationResult {

    private final double distanceKm;
    private final double tollCost;

    public TripCalculationResult(double distanceKm, double tollCost) {
        this.distanceKm = distanceKm;
        this.tollCost = tollCost;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getTollCost() {
        return tollCost;
    }
}