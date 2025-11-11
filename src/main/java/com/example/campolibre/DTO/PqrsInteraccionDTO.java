package com.example.campolibre.DTO;

import lombok.Data;

@Data
public class PqrsInteraccionDTO {

    // Contenido de la respuesta o de la réplica
    private String contenido;

    // ID del usuario que está realizando la acción (Proveedor o Consumidor)
    private Long idUsuario;
}