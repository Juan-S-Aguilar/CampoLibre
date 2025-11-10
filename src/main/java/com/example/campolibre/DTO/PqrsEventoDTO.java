package com.example.campolibre.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PqrsEventoDTO {
    private Long id_pqrs_evento;
    private Long id_pqrs;
    private Long id_evento;
}