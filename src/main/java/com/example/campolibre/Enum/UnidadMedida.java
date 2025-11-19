package com.example.campolibre.Enum;

public enum UnidadMedida {
    UNIDAD("Unidad"),
    DOCENA("Docena"),
    LIBRA("Libra"),
    KILO("Kilo"),
    BOLSA_1KG("Bolsa 1 kg"),
    BOLSA_2KG("Bolsa 2 kg"),
    PAQUETE("Paquete"),
    COMBO("Combo");

    private final String displayName;

    UnidadMedida(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}