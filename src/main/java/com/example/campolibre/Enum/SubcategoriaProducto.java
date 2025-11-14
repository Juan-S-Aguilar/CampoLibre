package com.example.campolibre.Enum;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum SubcategoriaProducto {
    // LÁCTEOS
    LECHE_ENTERA("Leche Entera", CategoriaProducto.LACTEOS),
    LECHE_DESCREMADA("Leche Descremada", CategoriaProducto.LACTEOS),
    LECHE_DESLACTOSADA("Leche Deslactosada", CategoriaProducto.LACTEOS),
    QUESO_FRESCO("Queso Fresco", CategoriaProducto.LACTEOS),
    QUESO_MADURADO("Queso Madurado", CategoriaProducto.LACTEOS),
    YOGURT("Yogurt", CategoriaProducto.LACTEOS),
    MANTEQUILLA("Mantequilla", CategoriaProducto.LACTEOS),
    CREMA("Crema de Leche", CategoriaProducto.LACTEOS),
    KUMIS("Kumis", CategoriaProducto.LACTEOS),

    // FRUTAS
    CITRICOS("Cítricos", CategoriaProducto.FRUTAS),
    TROPICALES("Tropicales", CategoriaProducto.FRUTAS),
    ANDINAS("Andinas", CategoriaProducto.FRUTAS),
    BERRIES("Berries y Frutos del Bosque", CategoriaProducto.FRUTAS),
    FRUTAS_DESHIDRATADAS("Frutas Deshidratadas", CategoriaProducto.FRUTAS),

    // VERDURAS
    HORTALIZAS_HOJA("Hortalizas de Hoja", CategoriaProducto.VERDURAS),
    TUBERCULOS("Tubérculos", CategoriaProducto.VERDURAS),
    RAICES("Raíces", CategoriaProducto.VERDURAS),
    HORTALIZAS_FRUTO("Hortalizas de Fruto", CategoriaProducto.VERDURAS),
    VERDURAS_DESHIDRATADAS("Verduras Deshidratadas", CategoriaProducto.VERDURAS),

    // GRANOS
    FRIJOLES("Frijoles", CategoriaProducto.GRANOS),
    LENTEJAS("Lentejas", CategoriaProducto.GRANOS),
    GARBANZOS("Garbanzos", CategoriaProducto.GRANOS),
    ARVEJAS("Arvejas", CategoriaProducto.GRANOS),
    HABAS("Habas", CategoriaProducto.GRANOS),

    // CEREALES
    ARROZ("Arroz", CategoriaProducto.CEREALES),
    MAIZ("Maíz", CategoriaProducto.CEREALES),
    TRIGO("Trigo", CategoriaProducto.CEREALES),
    AVENA("Avena", CategoriaProducto.CEREALES),
    QUINUA("Quinua", CategoriaProducto.CEREALES),
    HARINAS("Harinas", CategoriaProducto.CEREALES),

    // MERMELADAS
    MERMELADA_FRUTAS("Mermeladas de Frutas", CategoriaProducto.MERMELADAS),
    JALEAS("Jaleas", CategoriaProducto.MERMELADAS),
    COMPOTAS("Compotas", CategoriaProducto.MERMELADAS),
    DULCES("Dulces Artesanales", CategoriaProducto.MERMELADAS),

    // MIELES
    MIEL_ABEJA("Miel de Abeja", CategoriaProducto.MIELES),
    MIEL_PANELA("Miel de Panela", CategoriaProducto.MIELES),
    POLEN("Polen", CategoriaProducto.MIELES),
    PROPOLEO("Propóleo", CategoriaProducto.MIELES),

    // SEMILLAS
    SEMILLAS_CHIA("Semillas de Chía", CategoriaProducto.SEMILLAS),
    SEMILLAS_GIRASOL("Semillas de Girasol", CategoriaProducto.SEMILLAS),
    ALMENDRAS("Almendras", CategoriaProducto.SEMILLAS),
    NUECES("Nueces", CategoriaProducto.SEMILLAS),
    MARAÑON("Marañón", CategoriaProducto.SEMILLAS),
    AJONJOLI("Ajonjolí", CategoriaProducto.SEMILLAS);

    private final String nombre;
    private final CategoriaProducto categoriaProducto;

    SubcategoriaProducto(String nombre, CategoriaProducto categoriaProducto) {
        this.nombre = nombre;
        this.categoriaProducto = categoriaProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public CategoriaProducto getCategoriaProducto() {
        return categoriaProducto;
    }

    /**
     * Obtiene todas las subcategorías de una categoría específica
     */
    public static List<SubcategoriaProducto> getSubcategoriasPorCategoria(CategoriaProducto categoria) {
        return Arrays.stream(SubcategoriaProducto.values())
                .filter(sub -> sub.getCategoriaProducto() == categoria)
                .collect(Collectors.toList());
    }
}