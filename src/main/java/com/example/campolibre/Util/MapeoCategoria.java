package com.example.campolibre.Util;

import com.example.campolibre.Enum.CategoriaTienda;
import com.example.campolibre.Enum.SubcategoriaProducto;
import java.util.*;

public class MapeoCategoria {

    private static final Map<CategoriaTienda, Set<SubcategoriaProducto>> MAPEO_CATEGORIAS;

    static {
        MAPEO_CATEGORIAS = new HashMap<>();

        // FRUTAS Y VERDURAS
        MAPEO_CATEGORIAS.put(CategoriaTienda.FRUTAS_VERDURAS, EnumSet.of(
                SubcategoriaProducto.FRUTAS_TROPICALES,
                SubcategoriaProducto.FRUTAS_CITRICAS,
                SubcategoriaProducto.VERDURAS_HOJA,
                SubcategoriaProducto.TUBERCULOS,
                SubcategoriaProducto.HORTALIZAS
        ));

        // PROCESADOS ARTESANALES
        MAPEO_CATEGORIAS.put(CategoriaTienda.PROCESADOS_ARTESANALES, EnumSet.of(
                SubcategoriaProducto.MERMELADAS,
                SubcategoriaProducto.QUESOS,
                SubcategoriaProducto.PAN_ARTESANAL,
                SubcategoriaProducto.SALSAS,
                SubcategoriaProducto.CONSERVAS
        ));

        // GRANOS Y SEMILLAS
        MAPEO_CATEGORIAS.put(CategoriaTienda.GRANOS_SEMILLAS, EnumSet.of(
                SubcategoriaProducto.GRANOS_SECOS,
                SubcategoriaProducto.CEREALES,
                SubcategoriaProducto.SEMILLAS,
                SubcategoriaProducto.HARINAS_ARTESANALES,
                SubcategoriaProducto.LEGUMINOSAS
        ));

        // HIERBAS Y ESPECIAS
        MAPEO_CATEGORIAS.put(CategoriaTienda.HIERBAS_ESPECIAS, EnumSet.of(
                SubcategoriaProducto.HIERBAS_AROMATICAS,
                SubcategoriaProducto.ESPECIAS_SECAS,
                SubcategoriaProducto.MEZCLAS_CONDIMENTOS,
                SubcategoriaProducto.INFUSIONES_TES
        ));
    }

    /**
     * Verifica si una subcategoría es válida para la categoría de tienda
     */
    public static boolean esSubcategoriaValida(
            CategoriaTienda categoriaTienda,
            SubcategoriaProducto subcategoriaProducto) {

        if (categoriaTienda == null || subcategoriaProducto == null) {
            return false;
        }

        Set<SubcategoriaProducto> subcategoriasPermitidas = MAPEO_CATEGORIAS.get(categoriaTienda);
        return subcategoriasPermitidas != null && subcategoriasPermitidas.contains(subcategoriaProducto);
    }

    /**
     * Obtiene las subcategorías permitidas para una categoría de tienda
     */
    public static Set<SubcategoriaProducto> obtenerSubcategoriasPermitidas(
            CategoriaTienda categoriaTienda) {

        return MAPEO_CATEGORIAS.getOrDefault(categoriaTienda, Collections.emptySet());
    }

    /**
     * Obtiene lista de subcategorías para mostrar en vistas (ordenadas)
     */
    public static List<SubcategoriaProducto> obtenerSubcategoriasOrdenadas(
            CategoriaTienda categoriaTienda) {

        Set<SubcategoriaProducto> subcategorias = obtenerSubcategoriasPermitidas(categoriaTienda);
        List<SubcategoriaProducto> lista = new ArrayList<>(subcategorias);
        // Ya están en el orden del ENUM
        return lista;
    }

    /**
     * Verifica si la categoría existe en el mapeo
     */
    public static boolean existeCategoria(CategoriaTienda categoriaTienda) {
        return MAPEO_CATEGORIAS.containsKey(categoriaTienda);
    }
}