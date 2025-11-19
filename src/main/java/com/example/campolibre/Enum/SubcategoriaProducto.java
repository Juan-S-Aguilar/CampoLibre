package com.example.campolibre.Enum;

public enum SubcategoriaProducto {
    // Subcategorías de FRUTAS_VERDURAS
    FRUTAS_TROPICALES("Frutas Tropicales", CategoriaTienda.FRUTAS_VERDURAS),
    FRUTAS_CITRICAS("Frutas Cítricas", CategoriaTienda.FRUTAS_VERDURAS),
    VERDURAS_HOJA("Verduras de Hoja", CategoriaTienda.FRUTAS_VERDURAS),
    TUBERCULOS("Tubérculos", CategoriaTienda.FRUTAS_VERDURAS),
    HORTALIZAS("Hortalizas", CategoriaTienda.FRUTAS_VERDURAS),

    // Subcategorías de PROCESADOS_ARTESANALES
    MERMELADAS("Mermeladas", CategoriaTienda.PROCESADOS_ARTESANALES),
    QUESOS("Quesos", CategoriaTienda.PROCESADOS_ARTESANALES),
    PAN_ARTESANAL("Pan Artesanal", CategoriaTienda.PROCESADOS_ARTESANALES),
    SALSAS("Salsas", CategoriaTienda.PROCESADOS_ARTESANALES),
    CONSERVAS("Conservas", CategoriaTienda.PROCESADOS_ARTESANALES),

    // Subcategorías de GRANOS_SEMILLAS
    GRANOS_SECOS("Granos Secos", CategoriaTienda.GRANOS_SEMILLAS),
    CEREALES("Cereales", CategoriaTienda.GRANOS_SEMILLAS),
    SEMILLAS("Semillas", CategoriaTienda.GRANOS_SEMILLAS),
    HARINAS_ARTESANALES("Harinas Artesanales", CategoriaTienda.GRANOS_SEMILLAS),
    LEGUMINOSAS("Leguminosas", CategoriaTienda.GRANOS_SEMILLAS),

    // Subcategorías de HIERBAS_ESPECIAS
    HIERBAS_AROMATICAS("Hierbas Aromáticas", CategoriaTienda.HIERBAS_ESPECIAS),
    ESPECIAS_SECAS("Especias Secas", CategoriaTienda.HIERBAS_ESPECIAS),
    MEZCLAS_CONDIMENTOS("Mezclas y Condimentos", CategoriaTienda.HIERBAS_ESPECIAS),
    INFUSIONES_TES("Infusiones y Tés", CategoriaTienda.HIERBAS_ESPECIAS);

    private final String displayName;
    private final CategoriaTienda categoriaPadre;

    SubcategoriaProducto(String displayName, CategoriaTienda categoriaPadre) {
        this.displayName = displayName;
        this.categoriaPadre = categoriaPadre;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CategoriaTienda getCategoriaPadre() {
        return categoriaPadre;
    }

    /**
     * Verifica si esta subcategoría pertenece a la categoría de tienda especificada
     */
    public boolean perteneceA(CategoriaTienda categoria) {
        return this.categoriaPadre == categoria;
    }
}