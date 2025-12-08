package com.example.campolibre.Enum;

public enum EntidadBancaria {
    // Para Tarjetas de Crédito/Débito
    BANCOLOMBIA("Bancolombia"),
    DAVIVIENDA("Davivienda"),
    BBVA("BBVA Colombia"),

    // Para PSE
    NEQUI("Nequi"),
    DAVIPLATA("DaviPlata");

    private final String displayName;

    EntidadBancaria(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Métodos helper
    public boolean esParaTarjeta() {
        return this == BANCOLOMBIA || this == DAVIVIENDA || this == BBVA;
    }

    public boolean esParaPSE() {
        return this == NEQUI || this == DAVIPLATA;
    }
}
