package com.example.campolibre.Service;

import com.example.campolibre.DTO.PqrsReporteItemDTO;
import com.example.campolibre.Entity.Pqrs;
import com.example.campolibre.Entity.PqrsEvento;
import com.example.campolibre.Entity.PqrsTienda;
import com.example.campolibre.Repository.PqrsEventoRepository;
import com.example.campolibre.Repository.PqrsTiendaRepository;
import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.campolibre.Entity.PqrsRespuesta;
import com.example.campolibre.Repository.PqrsRespuestaRepository;
import java.util.Optional;

@Service
public class PdfService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private PqrsTiendaRepository pqrsTiendaRepository;

    @Autowired
    private PqrsEventoRepository pqrsEventoRepository;

    @Autowired
    private PqrsRespuestaRepository pqrsRespuestaRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Genera un archivo PDF con el reporte de PQRS usando Thymeleaf + Flying Saucer
     * @param pqrsList Lista de PQRS a incluir en el reporte
     * @return ByteArrayInputStream con el archivo PDF generado
     */
    public ByteArrayInputStream generarReportePqrs(List<Pqrs> pqrsList) throws DocumentException, IOException {

        // Convertir a DTO con información de asociación
        List<PqrsReporteItemDTO> pqrsConAsociacion = convertirConAsociacion(pqrsList);

        // Preparar datos para la plantilla
        Context context = new Context();
        context.setVariable("pqrsList", pqrsConAsociacion);
        context.setVariable("fechaGeneracion", LocalDateTime.now().format(DATE_FORMATTER));
        context.setVariable("totalRegistros", pqrsList.size());
        context.setVariable("estadisticas", generarEstadisticas(pqrsList));

        // Renderizar HTML desde plantilla Thymeleaf
        String htmlContent = templateEngine.process("reportes/pqrs-reporte-pdf", context);

        // Convertir HTML a PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();
        renderer.createPDF(outputStream);
        outputStream.close();

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    /**
     * Convierte PQRS a DTO incluyendo información de asociación
     */
    private List<PqrsReporteItemDTO> convertirConAsociacion(List<Pqrs> pqrsList) {
        List<PqrsReporteItemDTO> resultado = new ArrayList<>();

        for (Pqrs pqrs : pqrsList) {
            PqrsReporteItemDTO dto = new PqrsReporteItemDTO();
            dto.setId_pqrs(pqrs.getId_pqrs());
            dto.setTipo(pqrs.getTipo());
            dto.setDescripcion(pqrs.getDescripcion());
            dto.setFecha_envio(pqrs.getFecha_envio());
            dto.setEstado(pqrs.getEstado());
            // dto.setFecha_respuesta(pqrs.getFecha_respuesta()); // Ya no se usan los campos directos de PQRS
            // dto.setRespuesta(pqrs.getRespuesta());

            if (pqrs.getEmisor() != null) {
                dto.setId_emisor(pqrs.getEmisor().getId_usuario());
            }
            if (pqrs.getReceptor() != null) {
                dto.setId_receptor(pqrs.getReceptor().getId_usuario());
            }

            // 💡 2. OBTENER LA ÚLTIMA INTERACCIÓN (NUEVA LÓGICA)
            obtenerUltimaInteraccion(pqrs, dto);

            // Determinar asociación
            dto.setAsociacion(determinarAsociacion(pqrs));

            resultado.add(dto);
        }

        return resultado;
    }

    /**
     * Helper para obtener la última respuesta del historial de trazabilidad.
     */
    private void obtenerUltimaInteraccion(Pqrs pqrs, PqrsReporteItemDTO dto) {
        // Usa el repositorio para buscar la respuesta/réplica más reciente
        Optional<PqrsRespuesta> ultimaInteraccion = pqrsRespuestaRepository
                .findFirstByPqrsOrderByFechaEmisionDesc(pqrs);

        if (ultimaInteraccion.isPresent()) {
            PqrsRespuesta r = ultimaInteraccion.get();
            // Asignar el contenido y la fecha de la última interacción del historial
            dto.setRespuesta(r.getContenido());
            dto.setFecha_respuesta(r.getFechaEmision());
        } else {
            dto.setRespuesta(null);
            dto.setFecha_respuesta(null);
        }
    }

    /**
     * Determina a qué está asociada la PQRS
     */
    private String determinarAsociacion(Pqrs pqrs) {
        try {
            // Verificar asociación con tienda
            PqrsTienda pt = pqrsTiendaRepository.findByPqrsId(pqrs.getId_pqrs());
            if (pt != null && pt.getTienda() != null) {
                return "Tienda: " + pt.getTienda().getNombre();
            }

            // Verificar asociación con evento
            PqrsEvento pe = pqrsEventoRepository.findByPqrsId(pqrs.getId_pqrs());
            if (pe != null && pe.getEvento() != null) {
                return "Evento: " + pe.getEvento().getNombre();
            }
        } catch (Exception e) {
            // Si hay error, continuar
        }

        return "Administración General";
    }

    /**
     * Genera estadísticas del conjunto de PQRS
     */
    private Map<String, Object> generarEstadisticas(List<Pqrs> pqrsList) {
        Map<String, Object> stats = new HashMap<>();

        long pendientes = pqrsList.stream()
                .filter(p -> p.getEstado().name().equals("PENDIENTE"))
                .count();

        long respondidas = pqrsList.stream()
                .filter(p -> p.getEstado().name().equals("RESPONDIDA"))
                .count();

        long preguntas = pqrsList.stream()
                .filter(p -> p.getTipo().name().equals("PREGUNTA"))
                .count();

        long quejas = pqrsList.stream()
                .filter(p -> p.getTipo().name().equals("QUEJA"))
                .count();

        long sugerencias = pqrsList.stream()
                .filter(p -> p.getTipo().name().equals("SUGERENCIA"))
                .count();

        stats.put("pendientes", pendientes);
        stats.put("respondidas", respondidas);
        stats.put("preguntas", preguntas);
        stats.put("quejas", quejas);
        stats.put("sugerencias", sugerencias);

        return stats;
    }
}