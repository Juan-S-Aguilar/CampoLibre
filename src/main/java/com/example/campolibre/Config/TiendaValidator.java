package com.example.campolibre.Config;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validador de nombres de tiendas para prevenir nombres relacionados con:
 * - Carnes y productos cárnicos
 * - Animales vivos o crías
 * - Maquinaria agrícola
 * - Herramientas y equipos
 */
@Component
public class TiendaValidator {

    private static final List<String> PALABRAS_PROHIBIDAS_CARNES = Arrays.asList(
            "carne", "carnes", "carnivoro", "carniceria", "frigorífico", "frigorifico",
            "embutido", "embutidos", "chorizo", "salchicha", "jamón", "jamon",
            "mortadela", "longaniza", "morcilla", "butifarra",
            "res", "cerdo", "pollo", "pavo", "cordero", "ternera", "chuleta",
            "costilla", "lomo", "filete", "bistec", "hamburguesa"
    );

    private static final List<String> PALABRAS_PROHIBIDAS_ANIMALES = Arrays.asList(
            "ganado", "ganaderia", "ganadería", "crianza", "cría", "cria",
            "aves", "avícola", "avicola", "pollito", "pollitos", "cerdito", "cerditos",
            "ternero", "terneros", "cordero", "corderos", "cabrito", "cabritos",
            "pecuario", "pecuaria", "zootecnia", "veterinaria", "veterinario"
    );

    private static final List<String> PALABRAS_PROHIBIDAS_MAQUINARIA = Arrays.asList(
            "tractor", "tractores", "cosechadora", "trilladora", "arado",
            "cultivador", "sembradora", "motocultor", "guadaña", "guadañadora",
            "aspersora", "fumigadora", "bomba", "motor", "maquinaria",
            "implemento", "implementos", "equipo pesado"
    );

    private static final List<String> PALABRAS_PROHIBIDAS_HERRAMIENTAS = Arrays.asList(
            "pala", "azadon", "azadón", "rastrillo", "pico", "hacha",
            "machete", "hoz", "tijera de podar", "serrucho", "sierra",
            "martillo", "alicate", "llave", "destornillador", "ferreteria", "ferretería",
            "herramienta", "herramientas", "utensilio", "utensilios"
    );

    /**
     * Valida que el nombre de la tienda no contenga palabras prohibidas
     * @param nombreTienda Nombre a validar
     * @return true si el nombre es válido, false si contiene palabras prohibidas
     */
    public boolean esNombreValido(String nombreTienda) {
        if (nombreTienda == null || nombreTienda.trim().isEmpty()) {
            return false;
        }

        String nombreLower = nombreTienda.toLowerCase().trim();

        // Verificar cada lista de palabras prohibidas
        return !contieneAlgunaPalabra(nombreLower, PALABRAS_PROHIBIDAS_CARNES) &&
                !contieneAlgunaPalabra(nombreLower, PALABRAS_PROHIBIDAS_ANIMALES) &&
                !contieneAlgunaPalabra(nombreLower, PALABRAS_PROHIBIDAS_MAQUINARIA) &&
                !contieneAlgunaPalabra(nombreLower, PALABRAS_PROHIBIDAS_HERRAMIENTAS);
    }

    /**
     * Obtiene el mensaje de error específico indicando qué tipo de palabra prohibida se encontró
     */
    public String getMensajeError(String nombreTienda) {
        if (nombreTienda == null || nombreTienda.trim().isEmpty()) {
            return "El nombre de la tienda no puede estar vacío";
        }

        String nombreLower = nombreTienda.toLowerCase().trim();

        if (contieneAlgunaPalabra(nombreLower, PALABRAS_PROHIBIDAS_CARNES)) {
            return "El nombre no puede contener referencias a carnes o productos cárnicos. " +
                    "Este marketplace solo comercializa productos agrícolas alimenticios.";
        }

        if (contieneAlgunaPalabra(nombreLower, PALABRAS_PROHIBIDAS_ANIMALES)) {
            return "El nombre no puede contener referencias a animales vivos o crianza. " +
                    "Este marketplace solo comercializa productos agrícolas alimenticios.";
        }

        if (contieneAlgunaPalabra(nombreLower, PALABRAS_PROHIBIDAS_MAQUINARIA)) {
            return "El nombre no puede contener referencias a maquinaria agrícola. " +
                    "Este marketplace solo comercializa productos agrícolas alimenticios.";
        }

        if (contieneAlgunaPalabra(nombreLower, PALABRAS_PROHIBIDAS_HERRAMIENTAS)) {
            return "El nombre no puede contener referencias a herramientas o equipos. " +
                    "Este marketplace solo comercializa productos agrícolas alimenticios.";
        }

        return null;
    }

    /**
     * Verifica si el texto contiene alguna palabra de la lista
     */
    private boolean contieneAlgunaPalabra(String texto, List<String> palabras) {
        for (String palabra : palabras) {
            // Usar regex para buscar la palabra completa (con límites de palabra)
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(palabra) + "\\b");
            if (pattern.matcher(texto).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Método auxiliar para agregar más palabras prohibidas dinámicamente si es necesario
     */
    public void agregarPalabraProhibida(String categoria, String palabra) {
        switch (categoria.toUpperCase()) {
            case "CARNES":
                PALABRAS_PROHIBIDAS_CARNES.add(palabra.toLowerCase());
                break;
            case "ANIMALES":
                PALABRAS_PROHIBIDAS_ANIMALES.add(palabra.toLowerCase());
                break;
            case "MAQUINARIA":
                PALABRAS_PROHIBIDAS_MAQUINARIA.add(palabra.toLowerCase());
                break;
            case "HERRAMIENTAS":
                PALABRAS_PROHIBIDAS_HERRAMIENTAS.add(palabra.toLowerCase());
                break;
        }
    }
}