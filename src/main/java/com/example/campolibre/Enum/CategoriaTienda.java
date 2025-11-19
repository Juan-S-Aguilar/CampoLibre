package com.example.campolibre.Enum;

public enum CategoriaTienda {
    FRUTAS_VERDURAS("Frutas y Verduras"),
    PROCESADOS_ARTESANALES("Procesados Artesanales"),
    GRANOS_SEMILLAS("Granos y Semillas"),
    HIERBAS_ESPECIAS("Hierbas y Especias");

    private final String displayName;

    CategoriaTienda(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}