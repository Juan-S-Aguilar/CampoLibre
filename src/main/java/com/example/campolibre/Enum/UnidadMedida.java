package com.example.campolibre.Enum;

public enum UnidadMedida {
    KILOGRAMO("Kilogramo", "kg"),
    LIBRA("Libra", "lb"),
    GRAMO("Gramo", "g"),
    LITRO("Litro", "L"),
    MILILITRO("Mililitro", "ml"),
    UNIDAD("Unidad", "u"),
    DOCENA("Docena", "doc"),
    BULTO("Bulto/Costal", "bulto"),
    CAJA("Caja", "caja"),
    BANDEJA("Bandeja", "bandeja");

    private final String nombre;
    private final String abreviatura;

    UnidadMedida(String nombre, String abreviatura) {
        this.nombre = nombre;
        this.abreviatura = abreviatura;
    }

    public String getNombre() {
        return nombre;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    /**
     * Devuelve un formato legible: "5 kg", "10 litros", etc.
     */
    public String formatearConCantidad(Double cantidad) {
        if (cantidad == null) return abreviatura;

        // Si la cantidad es un número entero, no mostrar decimales
        if (cantidad % 1 == 0) {
            return String.format("%d %s", cantidad.intValue(), abreviatura);
        }
        return String.format("%.2f %s", cantidad, abreviatura);
    }
}