package com.example.campolibre.Enum;

public enum TipoPersonaPSE {
    NATURAL("Persona Natural"),
    JURIDICA("Persona Jurídica");

    private final String displayName;

    TipoPersonaPSE(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
