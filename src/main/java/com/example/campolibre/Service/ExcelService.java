package com.example.campolibre.Service;

import com.example.campolibre.DTO.PqrsReporteItemDTO;
import com.example.campolibre.Entity.Pqrs;
import com.example.campolibre.Entity.PqrsEvento;
import com.example.campolibre.Entity.PqrsTienda;
import com.example.campolibre.Repository.PqrsEventoRepository;
import com.example.campolibre.Repository.PqrsTiendaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import com.example.campolibre.Entity.PqrsRespuesta;
import com.example.campolibre.Repository.PqrsRespuestaRepository;

@Service
public class ExcelService {

    @Autowired
    private PqrsTiendaRepository pqrsTiendaRepository;

    @Autowired
    private PqrsEventoRepository pqrsEventoRepository;

    @Autowired
    private PqrsRespuestaRepository pqrsRespuestaRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Genera un archivo Excel con el reporte de PQRS
     * @param pqrsList Lista de PQRS a incluir en el reporte
     * @return ByteArrayInputStream con el archivo Excel generado
     */
    public ByteArrayInputStream generarReportePqrs(List<Pqrs> pqrsList) throws IOException {

        // Convertir a DTO con información de asociación
        List<PqrsReporteItemDTO> pqrsConAsociacion = convertirConAsociacion(pqrsList);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Crear hoja
            Sheet sheet = workbook.createSheet("Reporte PQRS");

            // Estilos
            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle fechaStyle = crearEstiloFecha(workbook);
            CellStyle textoStyle = crearEstiloTexto(workbook);

            // Crear fila de encabezados
            Row headerRow = sheet.createRow(0);
            String[] columnas = {"ID", "Tipo", "Descripción", "Fecha Envío", "Estado",
                    "Fecha Respuesta", "Respuesta", "Emisor ID", "Receptor ID",
                    "Asociado a"};

            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos
            int rowNum = 1;
            for (PqrsReporteItemDTO pqrs : pqrsConAsociacion) {
                Row row = sheet.createRow(rowNum++);

                // ID
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(pqrs.getId_pqrs());
                cell0.setCellStyle(textoStyle);

                // Tipo
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(pqrs.getTipo().toString());
                cell1.setCellStyle(textoStyle);

                // Descripción
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(pqrs.getDescripcion());
                cell2.setCellStyle(textoStyle);

                // Fecha Envío
                Cell cell3 = row.createCell(3);
                if (pqrs.getFecha_envio() != null) {
                    cell3.setCellValue(pqrs.getFecha_envio().format(DATE_FORMATTER));
                } else {
                    cell3.setCellValue("N/A");
                }
                cell3.setCellStyle(fechaStyle);

                // Estado
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(pqrs.getEstado().toString());
                cell4.setCellStyle(textoStyle);

                // Fecha Respuesta
                Cell cell5 = row.createCell(5);
                if (pqrs.getFecha_respuesta() != null) {
                    cell5.setCellValue(pqrs.getFecha_respuesta().format(DATE_FORMATTER));
                } else {
                    cell5.setCellValue("N/A");
                }
                cell5.setCellStyle(fechaStyle);

                // Respuesta
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(pqrs.getRespuesta() != null ? pqrs.getRespuesta() : "Sin respuesta");
                cell6.setCellStyle(textoStyle);

                // Emisor ID
                Cell cell7 = row.createCell(7);
                if (pqrs.getId_emisor() != null) {
                    cell7.setCellValue(pqrs.getId_emisor());
                } else {
                    cell7.setCellValue("N/A");
                }
                cell7.setCellStyle(textoStyle);

                // Receptor ID
                Cell cell8 = row.createCell(8);
                if (pqrs.getId_receptor() != null) {
                    cell8.setCellValue(pqrs.getId_receptor());
                } else {
                    cell8.setCellValue("N/A");
                }
                cell8.setCellStyle(textoStyle);

                // Asociado a
                Cell cell9 = row.createCell(9);
                cell9.setCellValue(pqrs.getAsociacion());
                cell9.setCellStyle(textoStyle);
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
                // Asegurar un ancho mínimo
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
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
            // dto.setFecha_respuesta(pqrs.getFecha_respuesta()); // ELIMINAR O IGNORAR
            // dto.setRespuesta(pqrs.getRespuesta()); // ELIMINAR O IGNORAR

            if (pqrs.getEmisor() != null) {
                dto.setId_emisor(pqrs.getEmisor().getId_usuario());
            }
            // 💡 AJUSTE: El receptor es el responsable actual, pero podemos usar el emisor de la última respuesta
            if (pqrs.getReceptor() != null) {
                dto.setId_receptor(pqrs.getReceptor().getId_usuario());
            }

            // 💡 2. OBTENER LA ÚLTIMA INTERACCIÓN (CORRECCIÓN)
            obtenerUltimaInteraccion(pqrs, dto);

            // Determinar asociación
            dto.setAsociacion(determinarAsociacion(pqrs));

            resultado.add(dto);
        }

        return resultado;
    }

    // 💡 NUEVO MÉTODO HELPER para obtener la última respuesta del historial
    private void obtenerUltimaInteraccion(Pqrs pqrs, PqrsReporteItemDTO dto) {
        // Usa el repositorio para buscar la respuesta más reciente
        Optional<PqrsRespuesta> ultimaInteraccion = pqrsRespuestaRepository
                .findFirstByPqrsOrderByFechaEmisionDesc(pqrs);

        if (ultimaInteraccion.isPresent()) {
            PqrsRespuesta r = ultimaInteraccion.get();

            // Asignar el contenido y la fecha de la última interacción
            dto.setRespuesta(r.getContenido());
            dto.setFecha_respuesta(r.getFechaEmision());

            // Opcional: Si quieres el ID de la persona que *envió* la última interacción (proveedor o consumidor)
            // if (r.getEmisor() != null) {
            //     dto.setId_receptor(r.getEmisor().getId_usuario());
            // }

        } else {
            // Si no hay respuesta en el historial (solo está la PQRS inicial)
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
     * Crea estilo para los encabezados
     */
    private CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Crea estilo para fechas
     */
    private CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Crea estilo para texto normal
     */
    private CellStyle crearEstiloTexto(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}