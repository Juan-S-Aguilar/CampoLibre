package com.example.campolibre.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagoEventoEstadisticasDTO {
    private Long totalPagos;
    private Long pagosExitosos;
    private Long pagosPendientes;
    private Long pagosFallidos;
    private Double totalRecaudado;
    private Double montoPromedioPorPago;
}