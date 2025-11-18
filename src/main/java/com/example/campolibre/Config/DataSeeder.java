// ==================== DataSeeder.java - ACTUALIZADO ====================
package com.example.campolibre.Config;

import com.example.campolibre.Entity.*;
import com.example.campolibre.Enum.*;
import com.example.campolibre.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(2) // Se ejecuta después de DataLoader
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TiendaRepository tiendaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private MisEventosRepository misEventosRepository;

    @Autowired
    private PqrsRepository pqrsRepository;

    @Autowired
    private PqrsTiendaRepository pqrsTiendaRepository;

    @Autowired
    private PqrsEventoRepository pqrsEventoRepository;

    @Autowired
    private PqrsRespuestaRepository pqrsRespuestaRepository;

    @Autowired
    private PatrocinadorRepository patrocinadorRepository;

    @Autowired
    private InscripcionProveedorRepository inscripcionProveedorRepository;

    @Autowired
    private PagoEventoRepository pagoEventoRepository;

    @Override
    public void run(String... args) throws Exception {
        // Solo crear datos si no existen
        if (tiendaRepository.count() == 0) {
            System.out.println("📦 Iniciando carga de datos de prueba...");

            cargarPatrocinadores();
            cargarTiendas();
            cargarProductos();
            cargarEventos();
            cargarInscripcionesProveedores();
            cargarMisEventos();
            cargarPqrs();

            System.out.println("✅ Datos de prueba cargados exitosamente!");
        } else {
            System.out.println("ℹ️ Ya existen datos en la base de datos. Omitiendo seeder.");
        }
    }

    // ==================== NUEVO: CARGAR PATROCINADORES ====================
    private void cargarPatrocinadores() {
        System.out.println("\n🏢 Creando patrocinadores...");

        List<Patrocinador> patrocinadores = new ArrayList<>();

        // Patrocinador 1 - Alcaldía de Bogotá
        Patrocinador p1 = new Patrocinador();
        p1.setNombre("Alcaldía de Bogotá");
        p1.setDescripcion("Gobierno distrital comprometido con el desarrollo rural y el fortalecimiento del sector agrícola en Bogotá y la región.");
        p1.setLogoUrl("https://ejemplo.com/logos/alcaldia-bogota.png");
        p1.setContactoEmail("eventos@bogota.gov.co");
        p1.setTelefonoContacto("6013649090");
        p1.setSitioWeb("https://bogota.gov.co");
        p1.setActivo(true);
        patrocinadores.add(p1);

        // Patrocinador 2 - Ministerio de Agricultura
        Patrocinador p2 = new Patrocinador();
        p2.setNombre("Ministerio de Agricultura y Desarrollo Rural");
        p2.setDescripcion("Entidad rectora de las políticas agropecuarias en Colombia, promoviendo el desarrollo sostenible del campo.");
        p2.setLogoUrl("https://ejemplo.com/logos/minagricultura.png");
        p2.setContactoEmail("atencion@minagricultura.gov.co");
        p2.setTelefonoContacto("6013349800");
        p2.setSitioWeb("https://www.minagricultura.gov.co");
        p2.setActivo(true);
        patrocinadores.add(p2);

        // Patrocinador 3 - Bancóldex
        Patrocinador p3 = new Patrocinador();
        p3.setNombre("Bancóldex");
        p3.setDescripcion("Banco de desarrollo empresarial de Colombia, apoyando el crecimiento y la modernización del sector agrícola.");
        p3.setLogoUrl("https://ejemplo.com/logos/bancoldex.png");
        p3.setContactoEmail("contacto@bancoldex.com");
        p3.setTelefonoContacto("6013532700");
        p3.setSitioWeb("https://www.bancoldex.com");
        p3.setActivo(true);
        patrocinadores.add(p3);

        // Patrocinador 4 - Fedepanela
        Patrocinador p4 = new Patrocinador();
        p4.setNombre("Fedepanela");
        p4.setDescripcion("Federación Colombiana de Productores de Panela, fortaleciendo la cadena productiva panelera del país.");
        p4.setLogoUrl("https://ejemplo.com/logos/fedepanela.png");
        p4.setContactoEmail("info@fedepanela.org.co");
        p4.setTelefonoContacto("6012328484");
        p4.setSitioWeb("https://www.fedepanela.org.co");
        p4.setActivo(true);
        patrocinadores.add(p4);

        // Patrocinador 5 - Fenalce
        Patrocinador p5 = new Patrocinador();
        p5.setNombre("Fenalce");
        p5.setDescripcion("Federación Nacional de Cultivadores de Cereales y Leguminosas, impulsando la seguridad alimentaria en Colombia.");
        p5.setLogoUrl("https://ejemplo.com/logos/fenalce.png");
        p5.setContactoEmail("contacto@fenalce.org");
        p5.setTelefonoContacto("6013405090");
        p5.setSitioWeb("https://www.fenalce.org");
        p5.setActivo(true);
        patrocinadores.add(p5);

        // Patrocinador 6 - Procolombia
        Patrocinador p6 = new Patrocinador();
        p6.setNombre("ProColombia");
        p6.setDescripcion("Entidad encargada de promover las exportaciones, el turismo internacional y la inversión extranjera en el sector agrícola colombiano.");
        p6.setLogoUrl("https://ejemplo.com/logos/procolombia.png");
        p6.setContactoEmail("info@procolombia.co");
        p6.setTelefonoContacto("6015876000");
        p6.setSitioWeb("https://www.procolombia.co");
        p6.setActivo(true);
        patrocinadores.add(p6);

        patrocinadorRepository.saveAll(patrocinadores);
        System.out.println("✓ " + patrocinadores.size() + " patrocinadores creados exitosamente");
    }

    // ==================== ACTUALIZADO: CARGAR EVENTOS ====================
    private void cargarEventos() {
        System.out.println("\n📅 Creando eventos...");

        Usuario admin = usuarioRepository.findByEmail("admin@campolibre.com");
        if (admin == null) {
            System.err.println("⚠️ No se encontró el administrador. Omitiendo carga de eventos.");
            return;
        }

        // Obtener patrocinadores
        Patrocinador patAlcaldia = patrocinadorRepository.findById(1L).orElse(null);
        Patrocinador patMinAgricultura = patrocinadorRepository.findById(2L).orElse(null);
        Patrocinador patBancoldex = patrocinadorRepository.findById(3L).orElse(null);
        Patrocinador patFedepanela = patrocinadorRepository.findById(4L).orElse(null);
        Patrocinador patFenalce = patrocinadorRepository.findById(5L).orElse(null);
        Patrocinador patProcolombia = patrocinadorRepository.findById(6L).orElse(null);

        if (patAlcaldia == null) {
            System.err.println("⚠️ No se encontraron patrocinadores. Omitiendo carga de eventos.");
            return;
        }

        List<Evento> eventos = new ArrayList<>();

        // ========== EVENTO 1 - PUBLICADO (Feria) ==========
        Evento evento1 = new Evento();
        evento1.setNombre("Feria Agrícola Regional 2025");
        evento1.setDescripcion("Gran feria donde productores del campo mostrarán sus mejores productos: frutas, verduras, lácteos, panela y más. " +
                "Habrá degustaciones gratuitas, música folclórica en vivo, concursos de productos tradicionales y actividades educativas para toda la familia. " +
                "Ven y conoce directamente a los campesinos de nuestra región.");
        evento1.setUbicacion("Parque Principal de Fusagasugá");
        evento1.setDireccionCompleta("Carrera 7 # 5-40, Parque Principal, Fusagasugá, Cundinamarca");
        evento1.setCiudad("Fusagasugá");
        evento1.setFecha_evento(LocalDate.now().plusDays(15));
        evento1.setHora_evento(LocalTime.of(9, 0));
        evento1.setTipo_evento(TipoEvento.FERIA);
        evento1.setEstado(EstadoEvento.PUBLICADO);
        evento1.setFechaPublicacion(LocalDateTime.now().minusDays(5));
        evento1.setCreado_por(admin);
        evento1.setPatrocinador(patAlcaldia);
        evento1.setImagen_evento("https://ejemplo.com/eventos/feria-agricola-2025.jpg");
        evento1.setCuposMaximosProveedor(50);
        evento1.setCostoEspacio(80000.0);
        evento1.setCuposOcupados(0); // Se incrementará con inscripciones
        evento1.setTerminosCondiciones("1. El espacio asignado es de 3x3 metros con mesa y silla.\n" +
                "2. Los proveedores deben llegar 2 horas antes del inicio para la instalación.\n" +
                "3. Campo Libre no se hace responsable por pérdidas o daños de mercancía durante el evento.\n" +
                "4. Se debe mantener el espacio limpio y en orden durante y después del evento.\n" +
                "5. No se permiten ventas de productos no autorizados previamente.\n" +
                "6. El proveedor es responsable de sus propios permisos sanitarios según corresponda.");
        eventos.add(evento1);

        // ========== EVENTO 2 - PUBLICADO (Taller) ==========
        Evento evento2 = new Evento();
        evento2.setNombre("Taller de Agricultura Orgánica y Sostenible");
        evento2.setDescripcion("Aprende técnicas modernas de cultivo orgánico, manejo de compostaje, control biológico de plagas y certificación orgánica. " +
                "Taller práctico dirigido por expertos del SENA y la Universidad Nacional. Incluye certificado de participación, kit de semillas orgánicas " +
                "y manual de buenas prácticas agrícolas.");
        evento2.setUbicacion("Auditorio SENA, Centro Agroindustrial");
        evento2.setDireccionCompleta("Km 7 Vía Fusagasugá - Arbeláez, Centro Agroindustrial SENA");
        evento2.setCiudad("Fusagasugá");
        evento2.setFecha_evento(LocalDate.now().plusDays(7));
        evento2.setHora_evento(LocalTime.of(14, 0));
        evento2.setTipo_evento(TipoEvento.TALLER);
        evento2.setEstado(EstadoEvento.PUBLICADO);
        evento2.setFechaPublicacion(LocalDateTime.now().minusDays(10));
        evento2.setCreado_por(admin);
        evento2.setPatrocinador(patMinAgricultura);
        evento2.setImagen_evento("https://ejemplo.com/eventos/taller-agricultura-organica.jpg");
        evento2.setCuposMaximosProveedor(30);
        evento2.setCostoEspacio(35000.0);
        evento2.setCuposOcupados(0);
        evento2.setTerminosCondiciones("1. Taller de 4 horas de duración (2:00 PM - 6:00 PM).\n" +
                "2. Se requiere asistencia puntual para recibir certificado.\n" +
                "3. Los materiales y el kit de semillas están incluidos en el costo.\n" +
                "4. Campo Libre actúa como plataforma de difusión; el SENA es responsable del contenido académico.\n" +
                "5. No hay devoluciones una vez confirmada la inscripción.");
        eventos.add(evento2);

        // ========== EVENTO 3 - PUBLICADO (Festival) ==========
        Evento evento3 = new Evento();
        evento3.setNombre("Festival del Café y el Cacao Colombiano");
        evento3.setDescripcion("Celebración de dos de los productos más emblemáticos de Colombia. Cata de cafés especiales, chocolates artesanales, " +
                "demostraciones de barismo, concursos de preparación, charlas sobre denominación de origen y rutas turísticas del café. " +
                "Premios para los mejores productores y entrada libre al público.");
        evento3.setUbicacion("Centro de Convenciones Compensar");
        evento3.setDireccionCompleta("Avenida Carrera 68 # 49A - 47, Puente Aranda, Bogotá D.C.");
        evento3.setCiudad("Bogotá");
        evento3.setFecha_evento(LocalDate.now().plusDays(22));
        evento3.setHora_evento(LocalTime.of(10, 0));
        evento3.setTipo_evento(TipoEvento.FESTIVAL);
        evento3.setEstado(EstadoEvento.PUBLICADO);
        evento3.setFechaPublicacion(LocalDateTime.now().minusDays(3));
        evento3.setCreado_por(admin);
        evento3.setPatrocinador(patProcolombia);
        evento3.setImagen_evento("https://ejemplo.com/eventos/festival-cafe-cacao.jpg");
        evento3.setCuposMaximosProveedor(40);
        evento3.setCostoEspacio(120000.0);
        evento3.setCuposOcupados(0);
        evento3.setTerminosCondiciones("1. El espacio incluye stand equipado con iluminación y mobiliario básico.\n" +
                "2. Los productos deben contar con registro sanitario vigente (INVIMA).\n" +
                "3. Se permite la venta directa al público durante el evento.\n" +
                "4. Campo Libre no se responsabiliza por accidentes o pérdidas materiales.\n" +
                "5. Prohibido el uso de estufas o equipos que generen llama abierta sin autorización previa.\n" +
                "6. El proveedor debe contar con su propio seguro de responsabilidad civil.");
        eventos.add(evento3);

        // ========== EVENTO 4 - EN_CURSO (Mercado) ==========
        Evento evento4 = new Evento();
        evento4.setNombre("Mercado Campesino de Fin de Semana");
        evento4.setDescripcion("Mercado campesino semanal donde productores locales ofrecen frutas, verduras, huevos, lácteos y productos artesanales " +
                "directamente al consumidor. Precios justos, productos frescos y apoyo directo a las familias campesinas de la región.");
        evento4.setUbicacion("Plaza de Mercado Municipal - Soacha");
        evento4.setDireccionCompleta("Calle 13 # 6-20, Centro, Soacha, Cundinamarca");
        evento4.setCiudad("Soacha");
        evento4.setFecha_evento(LocalDate.now()); // HOY
        evento4.setHora_evento(LocalTime.of(7, 0));
        evento4.setTipo_evento(TipoEvento.MERCADO);
        evento4.setEstado(EstadoEvento.EN_CURSO);
        evento4.setFechaPublicacion(LocalDateTime.now().minusDays(7));
        evento4.setCreado_por(admin);
        evento4.setPatrocinador(patAlcaldia);
        evento4.setImagen_evento("https://ejemplo.com/eventos/mercado-campesino.jpg");
        evento4.setCuposMaximosProveedor(25);
        evento4.setCostoEspacio(25000.0);
        evento4.setCuposOcupados(0);
        evento4.setTerminosCondiciones("1. Mercado de 7:00 AM a 2:00 PM.\n" +
                "2. El espacio es un puesto de 2x2 metros sin mobiliario.\n" +
                "3. El proveedor debe traer sus propias mesas, carpas y elementos de exhibición.\n" +
                "4. Campo Libre no se hace responsable por robos o daños.\n" +
                "5. Se recomienda contar con cambio en efectivo y datáfono para ventas.");
        eventos.add(evento4);

        // ========== EVENTO 5 - PUBLICADO (Capacitación) ==========
        Evento evento5 = new Evento();
        evento5.setNombre("Capacitación en Transformación de Lácteos");
        evento5.setDescripcion("Curso intensivo sobre elaboración artesanal de quesos, yogurt, kumis y arequipe. " +
                "Aprende técnicas de procesamiento, empaque, etiquetado, conservación y comercialización de productos lácteos. " +
                "Incluye práctica en planta piloto y manual técnico.");
        evento5.setUbicacion("Instituto Técnico Agrícola - La Mesa");
        evento5.setDireccionCompleta("Vereda San Javier, Vía Principal Km 2, La Mesa, Cundinamarca");
        evento5.setCiudad("La Mesa");
        evento5.setFecha_evento(LocalDate.now().plusDays(12));
        evento5.setHora_evento(LocalTime.of(8, 0));
        evento5.setTipo_evento(TipoEvento.CAPACITACION);
        evento5.setEstado(EstadoEvento.PUBLICADO);
        evento5.setFechaPublicacion(LocalDateTime.now().minusDays(8));
        evento5.setCreado_por(admin);
        evento5.setPatrocinador(patFedepanela);
        evento5.setImagen_evento("https://ejemplo.com/eventos/capacitacion-lacteos.jpg");
        evento5.setCuposMaximosProveedor(20);
        evento5.setCostoEspacio(50000.0);
        evento5.setCuposOcupados(0);
        evento5.setTerminosCondiciones("1. Capacitación de día completo: 8:00 AM - 5:00 PM.\n" +
                "2. Incluye refrigerio y almuerzo.\n" +
                "3. Los participantes deben usar ropa cómoda y calzado cerrado.\n" +
                "4. Se entrega certificado de asistencia avalado por el Instituto.\n" +
                "5. Campo Libre no asume responsabilidad por accidentes durante prácticas.");
        eventos.add(evento5);

        // ========== EVENTO 6 - FINALIZADO (Exposición) ==========
        Evento evento6 = new Evento();
        evento6.setNombre("Exposición Nacional de Maquinaria Agrícola");
        evento6.setDescripcion("Exposición de las últimas tecnologías en maquinaria, equipos y herramientas para el agro. " +
                "Tractores, cosechadoras, sistemas de riego, drones agrícolas, invernaderos inteligentes y más. " +
                "Evento que reunió a más de 80 expositores nacionales e internacionales.");
        evento6.setUbicacion("Corferias - Pabellón 3");
        evento6.setDireccionCompleta("Carrera 37 # 24-67, Zona Industrial, Bogotá D.C.");
        evento6.setCiudad("Bogotá");
        evento6.setFecha_evento(LocalDate.now().minusDays(10)); // PASADO
        evento6.setHora_evento(LocalTime.of(9, 0));
        evento6.setTipo_evento(TipoEvento.EXPOSICION);
        evento6.setEstado(EstadoEvento.FINALIZADO);
        evento6.setFechaPublicacion(LocalDateTime.now().minusDays(30));
        evento6.setCreado_por(admin);
        evento6.setPatrocinador(patBancoldex);
        evento6.setImagen_evento("https://ejemplo.com/eventos/expo-maquinaria.jpg");
        evento6.setCuposMaximosProveedor(80);
        evento6.setCostoEspacio(350000.0);
        evento6.setCuposOcupados(0); // Se llenará con inscripciones históricas
        evento6.setTerminosCondiciones("1. Stand de 9 m² con paredes divisorias y alfombra.\n" +
                "2. Incluye conexión eléctrica básica (110V).\n" +
                "3. El montaje debe realizarse el día anterior (8:00 AM - 6:00 PM).\n" +
                "4. Campo Libre no se responsabiliza por daños a equipos expuestos.\n" +
                "5. Se requiere seguro de exhibición para maquinaria de alto valor.");
        eventos.add(evento6);

        // ========== EVENTO 7 - BORRADOR (Feria) ==========
        Evento evento7 = new Evento();
        evento7.setNombre("Feria de la Panela y Derivados");
        evento7.setDescripcion("Feria dedicada exclusivamente a los productores de panela, mieles, melcochas y otros derivados de la caña. " +
                "Se realizarán concursos de calidad, charlas sobre innovación en el sector panelero y rueda de negocios con compradores mayoristas.");
        evento7.setUbicacion("Coliseo Municipal de Villeta");
        evento7.setDireccionCompleta("Calle 5 # 6-40, Centro, Villeta, Cundinamarca");
        evento7.setCiudad("Villeta");
        evento7.setFecha_evento(LocalDate.now().plusDays(35));
        evento7.setHora_evento(LocalTime.of(9, 0));
        evento7.setTipo_evento(TipoEvento.FERIA);
        evento7.setEstado(EstadoEvento.BORRADOR); // Aún no publicado
        evento7.setFechaPublicacion(null);
        evento7.setCreado_por(admin);
        evento7.setPatrocinador(patFedepanela);
        evento7.setImagen_evento(null);
        evento7.setCuposMaximosProveedor(35);
        evento7.setCostoEspacio(60000.0);
        evento7.setCuposOcupados(0);
        evento7.setTerminosCondiciones("Términos y condiciones en revisión. Pendiente de publicación oficial.");
        eventos.add(evento7);

        // ========== EVENTO 8 - CANCELADO (Taller) ==========
        Evento evento8 = new Evento();
        evento8.setNombre("Taller de Apicultura para Principiantes");
        evento8.setDescripcion("Curso básico sobre manejo de colmenas, producción de miel, cera y propóleo. " +
                "Desafortunadamente este evento fue cancelado por problemas logísticos del instructor principal.");
        evento8.setUbicacion("Finca Demostrativa El Colmenar");
        evento8.setDireccionCompleta("Vereda El Triunfo, Km 8 Vía Silvania, Cundinamarca");
        evento8.setCiudad("Silvania");
        evento8.setFecha_evento(LocalDate.now().plusDays(5));
        evento8.setHora_evento(LocalTime.of(9, 0));
        evento8.setTipo_evento(TipoEvento.TALLER);
        evento8.setEstado(EstadoEvento.CANCELADO);
        evento8.setFechaPublicacion(LocalDateTime.now().minusDays(15));
        evento8.setCreado_por(admin);
        evento8.setPatrocinador(patMinAgricultura);
        evento8.setImagen_evento("https://ejemplo.com/eventos/taller-apicultura.jpg");
        evento8.setCuposMaximosProveedor(15);
        evento8.setCostoEspacio(40000.0);
        evento8.setCuposOcupados(0);
        evento8.setTerminosCondiciones("EVENTO CANCELADO. Se realizará reembolso automático a los inscritos.");
        eventos.add(evento8);

        // ========== EVENTO 9 - PUBLICADO (Mercado) ==========
        Evento evento9 = new Evento();
        evento9.setNombre("Mercado Agroecológico de Bogotá");
        evento9.setDescripcion("Mercado especializado en productos 100% orgánicos y agroecológicos certificados. " +
                "Frutas, verduras, huevos, café orgánico, panela, miel y productos de aseo ecológicos. " +
                "Espacio para promover el consumo responsable y la agricultura libre de agroquímicos.");
        evento9.setUbicacion("Parque El Virrey");
        evento9.setDireccionCompleta("Calle 88 entre Carreras 11 y 15, Bogotá D.C.");
        evento9.setCiudad("Bogotá");
        evento9.setFecha_evento(LocalDate.now().plusDays(3));
        evento9.setHora_evento(LocalTime.of(8, 0));
        evento9.setTipo_evento(TipoEvento.MERCADO);
        evento9.setEstado(EstadoEvento.PUBLICADO);
        evento9.setFechaPublicacion(LocalDateTime.now().minusDays(12));
        evento9.setCreado_por(admin);
        evento9.setPatrocinador(patAlcaldia);
        evento9.setImagen_evento("https://ejemplo.com/eventos/mercado-agroecologico.jpg");
        evento9.setCuposMaximosProveedor(30);
        evento9.setCostoEspacio(45000.0);
        evento9.setCuposOcupados(0);
        evento9.setTerminosCondiciones("1. Mercado de 8:00 AM a 1:00 PM.\n" +
                "2. Solo se aceptan productos certificados orgánicos o en transición.\n" +
                "3. El proveedor debe presentar certificación o declaración de producción agroecológica.\n" +
                "4. Espacio al aire libre de 2x2 metros sin mobiliario.\n" +
                "5. Campo Libre no se responsabiliza por condiciones climáticas adversas.");
        eventos.add(evento9);

        // ========== EVENTO 10 - FINALIZADO (Festival) ==========
        Evento evento10 = new Evento();
        evento10.setNombre("Festival Gastronómico del Campo Colombiano");
        evento10.setDescripcion("Festival que celebró la riqueza culinaria del campo colombiano con platos típicos de todas las regiones. " +
                "Participaron más de 50 cocineros tradicionales mostrando ajiaco, bandeja paisa, sancocho, mote de queso y más. " +
                "Un evento exitoso que reunió a más de 5000 visitantes.");
        evento10.setUbicacion("Parque Simón Bolívar");
        evento10.setDireccionCompleta("Calle 63 # 50-00, Bogotá D.C.");
        evento10.setCiudad("Bogotá");
        evento10.setFecha_evento(LocalDate.now().minusDays(25)); // PASADO
        evento10.setHora_evento(LocalTime.of(11, 0));
        evento10.setTipo_evento(TipoEvento.FESTIVAL);
        evento10.setEstado(EstadoEvento.FINALIZADO);
        evento10.setFechaPublicacion(LocalDateTime.now().minusDays(45));
        evento10.setCreado_por(admin);
        evento10.setPatrocinador(patProcolombia);
        evento10.setImagen_evento("https://ejemplo.com/eventos/festival-gastronomico.jpg");
        evento10.setCuposMaximosProveedor(55);
        evento10.setCostoEspacio(95000.0);
        evento10.setCuposOcupados(0); // Se llenará con inscripciones
        evento10.setTerminosCondiciones("1. Stand de 4x4 metros con techo y conexión eléctrica.\n" +
                "2. Los cocineros deben contar con curso de manipulación de alimentos.\n" +
                "3. Campo Libre no se hace responsable por intoxicaciones alimentarias.\n" +
                "4. Cada participante debe traer sus propios utensilios y equipos de cocina.\n" +
                "5. Se permite venta de platos durante el evento.");
        eventos.add(evento10);

        eventoRepository.saveAll(eventos);
        System.out.println("✓ " + eventos.size() + " eventos creados exitosamente");
    }

    // ==================== NUEVO: CARGAR INSCRIPCIONES DE PROVEEDORES ====================
    private void cargarInscripcionesProveedores() {
        System.out.println("\n👥 Creando inscripciones de proveedores y pagos...");

        Usuario proveedor = usuarioRepository.findByEmail("proveedor@campolibre.com");
        if (proveedor == null) {
            System.err.println("⚠️ No se encontró el proveedor principal. Omitiendo inscripciones.");
            return;
        }

        // Buscar proveedores adicionales que puedan existir (si DataLoader los creó)
        List<Usuario> proveedores = new ArrayList<>();
        proveedores.add(proveedor);

        // Intentar obtener más proveedores si existen
        Usuario prov2 = usuarioRepository.findByEmail("proveedor2@campolibre.com");
        Usuario prov3 = usuarioRepository.findByEmail("proveedor3@campolibre.com");
        Usuario prov4 = usuarioRepository.findByEmail("proveedor4@campolibre.com");
        if (prov2 != null) proveedores.add(prov2);
        if (prov3 != null) proveedores.add(prov3);
        if (prov4 != null) proveedores.add(prov4);

        // Si solo existe un proveedor, trabajar solo con ese
        if (proveedores.size() == 1) {
            System.out.println("ℹ️ Solo existe un proveedor en el sistema. Creando inscripciones limitadas...");
        }

        // Obtener eventos
        Evento evento1 = eventoRepository.findById(1L).orElse(null); // Feria Agrícola - PUBLICADO
        Evento evento2 = eventoRepository.findById(2L).orElse(null); // Taller Agricultura - PUBLICADO
        Evento evento3 = eventoRepository.findById(3L).orElse(null); // Festival Café - PUBLICADO
        Evento evento4 = eventoRepository.findById(4L).orElse(null); // Mercado Campesino - EN_CURSO
        Evento evento5 = eventoRepository.findById(5L).orElse(null); // Capacitación Lácteos - PUBLICADO
        Evento evento6 = eventoRepository.findById(6L).orElse(null); // Exposición Maquinaria - FINALIZADO
        Evento evento9 = eventoRepository.findById(9L).orElse(null); // Mercado Agroecológico - PUBLICADO
        Evento evento10 = eventoRepository.findById(10L).orElse(null); // Festival Gastronómico - FINALIZADO

        if (evento1 == null) {
            System.err.println("⚠️ No se encontraron eventos. Omitiendo inscripciones.");
            return;
        }

        int inscripcionesCreadas = 0;

        // ========== INSCRIPCIONES PARA EVENTO 1 - Feria Agrícola ==========
        // Inscripción 1 - CONFIRMADA (Proveedor 1)
        inscripcionesCreadas += crearInscripcionConPago(
                proveedores.get(0),
                evento1,
                EstadoCupo.CONFIRMADO,
                EstadoPago.EXITOSO,
                MetodoPago.PSE,
                null
        );

        // Inscripción 2 - CONFIRMADA (Proveedor 2, si existe)
        if (proveedores.size() > 1) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(1),
                    evento1,
                    EstadoCupo.CONFIRMADO,
                    EstadoPago.EXITOSO,
                    MetodoPago.TARJETA_CREDITO,
                    null
            );
        }

        // Inscripción 3 - CONFIRMADA (Proveedor 3, si existe)
        if (proveedores.size() > 2) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(2),
                    evento1,
                    EstadoCupo.CONFIRMADO,
                    EstadoPago.EXITOSO,
                    MetodoPago.TARJETA_DEBITO,
                    null
            );
        }

        // Inscripción 4 - PENDIENTE_PAGO (Proveedor 4, si existe)
        if (proveedores.size() > 3) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(3),
                    evento1,
                    EstadoCupo.PENDIENTE,
                    EstadoPago.PENDIENTE,
                    MetodoPago.PSE,
                    null
            );
        }

        // ========== INSCRIPCIONES PARA EVENTO 2 - Taller Agricultura ==========
        // Inscripción 5 - CONFIRMADA
        if (evento2 != null) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(0),
                    evento2,
                    EstadoCupo.CONFIRMADO,
                    EstadoPago.EXITOSO,
                    MetodoPago.EFECTIVO,
                    null
            );
        }

        // Inscripción 6 - CONFIRMADA (Proveedor 2, si existe)
        if (evento2 != null && proveedores.size() > 1) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(1),
                    evento2,
                    EstadoCupo.CONFIRMADO,
                    EstadoPago.EXITOSO,
                    MetodoPago.TARJETA_CREDITO,
                    null
            );
        }

        // ========== INSCRIPCIONES PARA EVENTO 3 - Festival Café ==========
        // Inscripción 7 - CONFIRMADA
        if (evento3 != null) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(0),
                    evento3,
                    EstadoCupo.CONFIRMADO,
                    EstadoPago.EXITOSO,
                    MetodoPago.PSE,
                    null
            );
        }

        // Inscripción 8 - CANCELADA (pago fallido)
        if (evento3 != null && proveedores.size() > 2) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(2),
                    evento3,
                    EstadoCupo.CANCELADO,
                    EstadoPago.FALLIDO,
                    MetodoPago.TARJETA_CREDITO,
                    "Tarjeta declinada - Fondos insuficientes"
            );
        }

        // ========== INSCRIPCIONES PARA EVENTO 4 - Mercado Campesino (EN_CURSO) ==========
        // Inscripción 9 - CONFIRMADA
        if (evento4 != null) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(0),
                    evento4,
                    EstadoCupo.CONFIRMADO,
                    EstadoPago.EXITOSO,
                    MetodoPago.EFECTIVO,
                    null
            );
        }

        // Inscripción 10 - CONFIRMADA (Proveedor 3, si existe)
        if (evento4 != null && proveedores.size() > 2) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(2),
                    evento4,
                    EstadoCupo.CONFIRMADO,
                    EstadoPago.EXITOSO,
                    MetodoPago.PSE,
                    null
            );
        }

        // ========== INSCRIPCIONES PARA EVENTO 5 - Capacitación Lácteos ==========
        // Inscripción 11 - CANCELADA (pago fallido)
        if (evento5 != null && proveedores.size() > 1) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(1),
                    evento5,
                    EstadoCupo.CANCELADO,
                    EstadoPago.FALLIDO,
                    MetodoPago.TARJETA_DEBITO,
                    "Error de comunicación con banco emisor"
            );
        }

        // Inscripción 12 - PENDIENTE_PAGO
        if (evento5 != null && proveedores.size() > 3) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(3),
                    evento5,
                    EstadoCupo.PENDIENTE,
                    EstadoPago.PENDIENTE,
                    MetodoPago.PSE,
                    null
            );
        }

        // ========== INSCRIPCIONES PARA EVENTO 6 - Exposición (FINALIZADO) ==========
        // Inscripción 13 - CONFIRMADA (evento pasado)
        if (evento6 != null) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(0),
                    evento6,
                    EstadoCupo.CONFIRMADO,
                    EstadoPago.EXITOSO,
                    MetodoPago.TARJETA_CREDITO,
                    null
            );
        }

        // Inscripción 14 - CONFIRMADA (Proveedor 2, si existe)
        if (evento6 != null && proveedores.size() > 1) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(1),
                    evento6,
                    EstadoCupo.CONFIRMADO,
                    EstadoPago.EXITOSO,
                    MetodoPago.PSE,
                    null
            );
        }

        // ========== INSCRIPCIONES PARA EVENTO 9 - Mercado Agroecológico ==========
        // Inscripción 15 - CONFIRMADA
        if (evento9 != null && proveedores.size() > 2) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(2),
                    evento9,
                    EstadoCupo.CONFIRMADO,
                    EstadoPago.EXITOSO,
                    MetodoPago.TARJETA_DEBITO,
                    null
            );
        }

        // Inscripción 16 - CONFIRMADA (Proveedor 3, si existe)
        if (evento9 != null && proveedores.size() > 3) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(3),
                    evento9,
                    EstadoCupo.CONFIRMADO,
                    EstadoPago.EXITOSO,
                    MetodoPago.EFECTIVO,
                    null
            );
        }

        // ========== INSCRIPCIONES PARA EVENTO 10 - Festival Gastronómico (FINALIZADO) ==========
        // Inscripción 17 - CONFIRMADA (evento pasado)
        if (evento10 != null) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(0),
                    evento10,
                    EstadoCupo.CONFIRMADO,
                    EstadoPago.EXITOSO,
                    MetodoPago.PSE,
                    null
            );
        }

        // Inscripción 18 - CONFIRMADA (Proveedor 2, si existe)
        if (evento10 != null && proveedores.size() > 1) {
            inscripcionesCreadas += crearInscripcionConPago(
                    proveedores.get(1),
                    evento10,
                    EstadoCupo.CONFIRMADO,
                    EstadoPago.EXITOSO,
                    MetodoPago.TARJETA_CREDITO,
                    null
            );
        }

        System.out.println("✓ " + inscripcionesCreadas + " inscripciones y pagos creados exitosamente");

        // Verificar consistencia de cupos
        verificarConsistenciaCupos();
    }

    /**
     * Método helper para crear inscripción con su pago asociado
     * Retorna 1 si se creó exitosamente, 0 si hubo error
     */
    private int crearInscripcionConPago(Usuario proveedor, Evento evento, EstadoCupo estadoCupo,
                                        EstadoPago estadoPago, MetodoPago metodoPago, String mensajeError) {
        try {
            // 1. Crear inscripción
            InscripcionProveedor inscripcion = new InscripcionProveedor();
            inscripcion.setProveedor(proveedor);
            inscripcion.setEvento(evento);
            inscripcion.setEstadoCupo(estadoCupo);
            inscripcion.setCostoPagado(evento.getCostoEspacio());

            // Solo setear fechaConfirmacion si está CONFIRMADO
            if (estadoCupo == EstadoCupo.CONFIRMADO) {
                inscripcion.setFechaConfirmacion(LocalDateTime.now().minusDays(Math.abs(evento.getFecha_evento().toEpochDay() - LocalDate.now().toEpochDay()) / 2));
            }

            // Guardar inscripción (esto genera codigoConfirmacion automáticamente)
            InscripcionProveedor inscripcionGuardada = inscripcionProveedorRepository.save(inscripcion);

            // 2. Crear pago asociado
            PagoEvento pago = new PagoEvento();
            pago.setInscripcionProveedor(inscripcionGuardada);
            pago.setMonto(evento.getCostoEspacio());
            pago.setMetodoPago(metodoPago);
            pago.setEstado(estadoPago);

            // Solo setear mensajeError si el pago es FALLIDO
            if (estadoPago == EstadoPago.FALLIDO && mensajeError != null) {
                pago.setMensajeError(mensajeError);
            }

            // Guardar pago (esto genera numeroTransaccion automáticamente)
            PagoEvento pagoGuardado = pagoEventoRepository.save(pago);

            // 3. Actualizar inscripción con referencia al pago
            inscripcionGuardada.setPagoEvento(pagoGuardado);
            inscripcionProveedorRepository.save(inscripcionGuardada);

            // 4. Si el pago es EXITOSO, incrementar cupos del evento
            if (estadoPago == EstadoPago.EXITOSO) {
                evento.setCuposOcupados(evento.getCuposOcupados() + 1);
                eventoRepository.save(evento);
            }

            return 1; // Éxito
        } catch (Exception e) {
            System.err.println("⚠️ Error al crear inscripción: " + e.getMessage());
            return 0; // Error
        }
    }

    /**
     * Verificar que los cuposOcupados coincidan con las inscripciones CONFIRMADAS
     */
    private void verificarConsistenciaCupos() {
        System.out.println("\n🔍 Verificando consistencia de cupos...");

        List<Evento> eventos = eventoRepository.findAll();
        boolean hayInconsistencias = false;

        for (Evento evento : eventos) {
            Long cuposConfirmados = inscripcionProveedorRepository
                    .countByEventoIdAndEstadoCupo(evento.getId_evento(), EstadoCupo.CONFIRMADO);

            if (!cuposConfirmados.equals((long) evento.getCuposOcupados())) {
                System.err.println("⚠️ INCONSISTENCIA en evento: " + evento.getNombre());
                System.err.println("   - Cupos ocupados en evento: " + evento.getCuposOcupados());
                System.err.println("   - Inscripciones confirmadas: " + cuposConfirmados);
                hayInconsistencias = true;
            }
        }

        if (!hayInconsistencias) {
            System.out.println("✓ Todos los cupos están consistentes");
        }
    }

    // ==================== ACTUALIZADO: CARGAR MIS EVENTOS ====================
    private void cargarMisEventos() {
        System.out.println("\n⭐ Creando registros de Mis Eventos (consumidores)...");

        Usuario consumidor = usuarioRepository.findByEmail("consumidor@campolibre.com");
        if (consumidor == null) {
            System.err.println("⚠️ No se encontró el consumidor. Omitiendo Mis Eventos.");
            return;
        }

        // Buscar consumidores adicionales si existen
        List<Usuario> consumidores = new ArrayList<>();
        consumidores.add(consumidor);

        Usuario cons2 = usuarioRepository.findByEmail("consumidor2@campolibre.com");
        Usuario cons3 = usuarioRepository.findByEmail("consumidor3@campolibre.com");
        if (cons2 != null) consumidores.add(cons2);
        if (cons3 != null) consumidores.add(cons3);

        // Obtener eventos PUBLICADOS y EN_CURSO (los únicos que consumidores pueden guardar)
        Evento evento1 = eventoRepository.findById(1L).orElse(null); // PUBLICADO
        Evento evento2 = eventoRepository.findById(2L).orElse(null); // PUBLICADO
        Evento evento3 = eventoRepository.findById(3L).orElse(null); // PUBLICADO
        Evento evento4 = eventoRepository.findById(4L).orElse(null); // EN_CURSO
        Evento evento9 = eventoRepository.findById(9L).orElse(null); // PUBLICADO

        if (evento1 == null) {
            System.err.println("⚠️ No se encontraron eventos. Omitiendo Mis Eventos.");
            return;
        }

        int misEventosCreados = 0;

        // Consumidor 1 guarda varios eventos
        if (evento1 != null) {
            misEventosCreados += crearMisEventos(consumidores.get(0), evento1, true);
        }
        if (evento3 != null) {
            misEventosCreados += crearMisEventos(consumidores.get(0), evento3, true);
        }
        if (evento9 != null) {
            misEventosCreados += crearMisEventos(consumidores.get(0), evento9, false);
        }

        // Consumidor 2 guarda eventos (si existe)
        if (consumidores.size() > 1) {
            if (evento2 != null) {
                misEventosCreados += crearMisEventos(consumidores.get(1), evento2, true);
            }
            if (evento4 != null) {
                misEventosCreados += crearMisEventos(consumidores.get(1), evento4, false);
            }
        }

        // Consumidor 3 guarda eventos (si existe)
        if (consumidores.size() > 2) {
            if (evento1 != null) {
                misEventosCreados += crearMisEventos(consumidores.get(2), evento1, true);
            }
            if (evento2 != null) {
                misEventosCreados += crearMisEventos(consumidores.get(2), evento2, false);
            }
        }

        System.out.println("✓ " + misEventosCreados + " registros de Mis Eventos creados");
    }

    /**
     * Método helper para crear registro de MisEventos
     * Retorna 1 si se creó exitosamente, 0 si hubo error
     */
    private int crearMisEventos(Usuario consumidor, Evento evento, boolean notificado) {
        try {
            MisEventos misEventos = new MisEventos();
            misEventos.setUsuario(consumidor);
            misEventos.setEvento(evento);
            misEventos.setNotificado(notificado);
            // fecha_guardado se genera automáticamente
            misEventosRepository.save(misEventos);
            return 1;
        } catch (Exception e) {
            System.err.println("⚠️ Error al crear MisEventos: " + e.getMessage());
            return 0;
        }
    }

    // ==================== TIENDAS (SIN CAMBIOS) ====================
    private void cargarTiendas() {
        System.out.println("\n🏪 Creando tiendas...");

        Usuario admin = usuarioRepository.findByEmail("admin@campolibre.com");
        Usuario proveedor = usuarioRepository.findByEmail("proveedor@campolibre.com");

        // Tienda 1 - Del proveedor
        Tienda tienda1 = new Tienda();
        tienda1.setNombre("Frutas del Valle");
        tienda1.setDescripcion("Frutas frescas y orgánicas directamente del campo. Cultivamos sin químicos y con amor por la naturaleza.");
        tienda1.setEstado(EstadoTienda.ACTIVA);
        tienda1.setCorreo_tienda("frutas@valle.com");
        tienda1.setTelefono_tienda("3101234567");
        tienda1.setUbicacion("Vereda El Paraíso, Municipio de Fusagasugá");
        tienda1.setUsuario(proveedor);
        tienda1.setImagen_tienda(null);
        tiendaRepository.save(tienda1);
        System.out.println("✓ Tienda creada: " + tienda1.getNombre());

        // Tienda 2 - Del proveedor
        Tienda tienda2 = new Tienda();
        tienda2.setNombre("Lácteos La Campiña");
        tienda2.setDescripcion("Productos lácteos artesanales: quesos, yogurt y mantequilla. Más de 20 años de tradición familiar.");
        tienda2.setEstado(EstadoTienda.ACTIVA);
        tienda2.setCorreo_tienda("lacteos@campina.com");
        tienda2.setTelefono_tienda("3209876543");
        tienda2.setUbicacion("Finca Los Rosales, Vía La Mesa");
        tienda2.setUsuario(proveedor);
        tienda2.setImagen_tienda(null);
        tiendaRepository.save(tienda2);
        System.out.println("✓ Tienda creada: " + tienda2.getNombre());

        // Tienda 3 - Del admin (para pruebas)
        Tienda tienda3 = new Tienda();
        tienda3.setNombre("Herramientas AgroTech");
        tienda3.setDescripcion("Herramientas y maquinaria agrícola de calidad. Asesoría técnica incluida.");
        tienda3.setEstado(EstadoTienda.ACTIVA);
        tienda3.setCorreo_tienda("ventas@agrotech.com");
        tienda3.setTelefono_tienda("3157654321");
        tienda3.setUbicacion("Centro Comercial Agrícola, Bogotá");
        tienda3.setUsuario(admin);
        tienda3.setImagen_tienda(null);
        tiendaRepository.save(tienda3);
        System.out.println("✓ Tienda creada: " + tienda3.getNombre());
    }

    // ==================== PRODUCTOS (SIN CAMBIOS) ====================
    private void cargarProductos() {
        System.out.println("\n📦 Creando productos...");

        Tienda tienda1 = tiendaRepository.findById(1L).orElse(null);
        Tienda tienda2 = tiendaRepository.findById(2L).orElse(null);
        Tienda tienda3 = tiendaRepository.findById(3L).orElse(null);

        if (tienda1 == null || tienda2 == null || tienda3 == null) {
            System.out.println("⚠️ No se encontraron tiendas para agregar productos");
            return;
        }

        // Productos de Tienda 1 (Frutas del Valle)
        Producto p1 = new Producto();
        p1.setNombre("Aguacate Hass");
        p1.setDescripcion("Aguacate de primera calidad, cremoso y nutritivo. Perfecto para ensaladas y preparaciones.");
        p1.setPrecio(2500.0);
        p1.setStock(50);
        p1.setCategoria(CategoriaProducto.FRUTAS);
        p1.setTienda(tienda1);
        p1.setImagen_producto(null);
        p1.setEstado("ACTIVO");
        productoRepository.save(p1);

        Producto p2 = new Producto();
        p2.setNombre("Mango Tommy");
        p2.setDescripcion("Mango dulce y jugoso, ideal para jugos y postres. Cosecha reciente.");
        p2.setPrecio(3000.0);
        p2.setStock(30);
        p2.setCategoria(CategoriaProducto.FRUTAS);
        p2.setTienda(tienda1);
        p2.setImagen_producto(null);
        p2.setEstado("ACTIVO");
        productoRepository.save(p2);

        Producto p3 = new Producto();
        p3.setNombre("Papaya Maradol");
        p3.setDescripcion("Papaya grande y dulce, rica en vitaminas. Excelente para el desayuno.");
        p3.setPrecio(4500.0);
        p3.setStock(20);
        p3.setCategoria(CategoriaProducto.FRUTAS);
        p3.setTienda(tienda1);
        p3.setImagen_producto(null);
        p3.setEstado("ACTIVO");
        productoRepository.save(p3);

        // Productos de Tienda 2 (Lácteos La Campiña)
        Producto p4 = new Producto();
        p4.setNombre("Queso Campesino");
        p4.setDescripcion("Queso fresco artesanal de 500g. Elaborado con leche del día, sin conservantes.");
        p4.setPrecio(12000.0);
        p4.setStock(25);
        p4.setCategoria(CategoriaProducto.LACTEOS);
        p4.setTienda(tienda2);
        p4.setImagen_producto(null);
        p4.setEstado("ACTIVO");
        productoRepository.save(p4);

        Producto p5 = new Producto();
        p5.setNombre("Yogurt Natural");
        p5.setDescripcion("Yogurt casero de 1 litro, sin azúcar añadida. Probióticos naturales.");
        p5.setPrecio(8000.0);
        p5.setStock(40);
        p5.setCategoria(CategoriaProducto.LACTEOS);
        p5.setTienda(tienda2);
        p5.setImagen_producto(null);
        p5.setEstado("ACTIVO");
        productoRepository.save(p5);

        Producto p6 = new Producto();
        p6.setNombre("Mantequilla Artesanal");
        p6.setDescripcion("Mantequilla casera de 250g. Sabor único y tradicional.");
        p6.setPrecio(6500.0);
        p6.setStock(15);
        p6.setCategoria(CategoriaProducto.LACTEOS);
        p6.setTienda(tienda2);
        p6.setImagen_producto(null);
        p6.setEstado("ACTIVO");
        productoRepository.save(p6);

        // Productos de Tienda 3 (Herramientas AgroTech)
        Producto p7 = new Producto();
        p7.setNombre("Pala Agrícola Reforzada");
        p7.setDescripcion("Pala de acero con mango de madera. Resistente y duradera.");
        p7.setPrecio(45000.0);
        p7.setStock(12);
        p7.setCategoria(CategoriaProducto.HERRAMIENTAS);
        p7.setTienda(tienda3);
        p7.setImagen_producto(null);
        p7.setEstado("ACTIVO");
        productoRepository.save(p7);

        Producto p8 = new Producto();
        p8.setNombre("Azadón Premium");
        p8.setDescripcion("Azadón de alta calidad con cabeza forjada. Ideal para labores pesadas.");
        p8.setPrecio(38000.0);
        p8.setStock(10);
        p8.setCategoria(CategoriaProducto.HERRAMIENTAS);
        p8.setTienda(tienda3);
        p8.setImagen_producto(null);
        p8.setEstado("ACTIVO");
        productoRepository.save(p8);

        Producto p9 = new Producto();
        p9.setNombre("Fumigadora Manual 20L");
        p9.setDescripcion("Fumigadora de espalda con capacidad de 20 litros. Boquilla ajustable.");
        p9.setPrecio(125000.0);
        p9.setStock(5);
        p9.setCategoria(CategoriaProducto.MAQUINARIA);
        p9.setTienda(tienda3);
        p9.setImagen_producto(null);
        p9.setEstado("ACTIVO");
        productoRepository.save(p9);

        System.out.println("✓ 9 productos creados exitosamente");
    }

    // ==================== PQRS (SIN CAMBIOS) ====================
    private void cargarPqrs() {
        System.out.println("\n📝 Creando PQRS...");

        Usuario consumidor = usuarioRepository.findByEmail("consumidor@campolibre.com");
        Usuario proveedor = usuarioRepository.findByEmail("proveedor@campolibre.com");
        Usuario admin = usuarioRepository.findByEmail("admin@campolibre.com");

        Tienda tienda1 = tiendaRepository.findById(1L).orElse(null);
        Evento evento1 = eventoRepository.findById(1L).orElse(null);

        if (consumidor == null || tienda1 == null || evento1 == null || admin == null) {
            System.out.println("⚠️ No se encontraron datos para crear PQRS");
            return;
        }

        // --- PQRS 1 - PENDIENTE (sobre una tienda) ---
        Pqrs pqrs1 = new Pqrs();
        pqrs1.setTipo(TipoPqrs.PREGUNTA);
        pqrs1.setDescripcion("¿Hacen envíos a domicilio? Me interesa comprar varios productos de su tienda pero vivo lejos.");
        pqrs1.setEstado(EstadoPqrs.PENDIENTE);
        pqrs1.setEmisor(consumidor);
        pqrs1.setReceptor(admin);
        pqrs1.setPendienteDe(RolProceso.PROVEEDOR);
        pqrsRepository.save(pqrs1);

        // Asociar con tienda
        PqrsTienda pt1 = new PqrsTienda();
        pt1.setPqrs(pqrs1);
        pt1.setTienda(tienda1);
        pqrsTiendaRepository.save(pt1);
        System.out.println("✓ PQRS creada: Pregunta sobre tienda (PENDIENTE)");

        // --- PQRS 2 - RESPONDIDA (sobre un evento) ---
        Pqrs pqrs2 = new Pqrs();
        pqrs2.setTipo(TipoPqrs.PREGUNTA);
        pqrs2.setDescripcion("¿La entrada al evento es gratuita o tiene algún costo?");
        pqrs2.setEstado(EstadoPqrs.RESPONDIDA);
        pqrs2.setEmisor(consumidor);
        pqrs2.setReceptor(consumidor);
        pqrs2.setPendienteDe(RolProceso.CONSUMIDOR);
        pqrsRepository.save(pqrs2);

        // Crear el registro de respuesta
        PqrsRespuesta resp2 = new PqrsRespuesta();
        resp2.setPqrs(pqrs2);
        resp2.setContenido("¡Hola! La entrada es completamente gratuita para todos los asistentes. Te esperamos.");
        resp2.setEmisor(admin);
        resp2.setEmitidoPor(RolProceso.PROVEEDOR);
        resp2.setFechaEmision(LocalDateTime.now().minusDays(1));
        pqrsRespuestaRepository.save(resp2);

        // Asociar con evento
        PqrsEvento pe1 = new PqrsEvento();
        pe1.setPqrs(pqrs2);
        pe1.setEvento(evento1);
        pqrsEventoRepository.save(pe1);
        System.out.println("✓ PQRS creada: Pregunta sobre evento (RESPONDIDA)");

        // --- PQRS 3 - RESPONDIDA (sugerencia general) ---
        Pqrs pqrs3 = new Pqrs();
        pqrs3.setTipo(TipoPqrs.SUGERENCIA);
        pqrs3.setDescripcion("Sería excelente si pudieran agregar filtros por precio en la búsqueda de productos. Facilitaría mucho encontrar lo que busco.");
        pqrs3.setEstado(EstadoPqrs.RESPONDIDA);
        pqrs3.setEmisor(proveedor);
        pqrs3.setReceptor(proveedor);
        pqrs3.setPendienteDe(RolProceso.CONSUMIDOR);
        pqrsRepository.save(pqrs3);

        // Crear el registro de respuesta
        PqrsRespuesta resp3 = new PqrsRespuesta();
        resp3.setPqrs(pqrs3);
        resp3.setContenido("¡Gracias por tu sugerencia! La tomaremos en cuenta para futuras actualizaciones de la plataforma.");
        resp3.setEmisor(admin);
        resp3.setEmitidoPor(RolProceso.PROVEEDOR);
        resp3.setFechaEmision(LocalDateTime.now().minusDays(2));
        pqrsRespuestaRepository.save(resp3);

        System.out.println("✓ PQRS creada: Sugerencia general (RESPONDIDA)");
    }
}