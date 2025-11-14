package com.example.campolibre.Enum;

public enum CategoriaProducto {
    LACTEOS("Lácteos", "Productos derivados de la leche"),
    FRUTAS("Frutas", "Frutas frescas y procesadas"),
    VERDURAS("Verduras", "Verduras y hortalizas frescas"),
    GRANOS("Granos", "Legumbres y granos secos"),
    CEREALES("Cereales", "Cereales y productos derivados"),
    MERMELADAS("Mermeladas", "Mermeladas y conservas dulces"),
    MIELES("Mieles", "Miel de abeja y productos apícolas"),
    SEMILLAS("Semillas", "Semillas comestibles y frutos secos");

    private final String nombre;
    private final String descripcion;

    CategoriaProducto(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}