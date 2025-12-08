// ==================== ComprehensiveDataSeeder.java ====================
// Seeder Unificado y Robusto para Campo Libre
// Crea datos de prueba completos para todos los módulos del sistema
// ========================================================================

package com.example.campolibre.Config;

import com.example.campolibre.Entity.*;
import com.example.campolibre.Enum.*;
import com.example.campolibre.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(1) // Ejecutar primero
public class ComprehensiveDataSeeder implements CommandLineRunner {

    // ==================== REPOSITORIOS ====================
    @Autowired private RolRepository rolRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PatrocinadorRepository patrocinadorRepository;
    @Autowired private EventoRepository eventoRepository;
    @Autowired private InscripcionProveedorRepository inscripcionProveedorRepository;
    @Autowired private PagoEventoRepository pagoEventoRepository;
    @Autowired private TiendaRepository tiendaRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private MisEventosRepository misEventosRepository;
    @Autowired private PqrsRepository pqrsRepository;
    @Autowired private PqrsTiendaRepository pqrsTiendaRepository;
    @Autowired private PqrsEventoRepository pqrsEventoRepository;
    @Autowired private PqrsRespuestaRepository pqrsRespuestaRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // ==================== LISTAS PARA ALMACENAR ENTIDADES CREADAS ====================
    private List<Usuario> administradores = new ArrayList<>();
    private List<Usuario> proveedores = new ArrayList<>();
    private List<Usuario> consumidores = new ArrayList<>();
    private List<Patrocinador> patrocinadores = new ArrayList<>();
    private List<Evento> eventos = new ArrayList<>();
    private List<Tienda> tiendas = new ArrayList<>();

    private Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        // Solo ejecutar si no hay datos
        if (usuarioRepository.count() > 0) {
            System.out.println("ℹ️  Ya existen datos en la base de datos. Omitiendo seeder.");
            System.out.println("💡 Para limpiar los datos, ejecuta: DELETE FROM usuario; (y todas las tablas relacionadas)");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   📦 INICIANDO CARGA DE DATOS DE PRUEBA - CAMPO LIBRE    ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        long startTime = System.currentTimeMillis();

        // Ejecutar carga de datos en orden
        crearRoles();
        crearUsuarios();
        crearPatrocinadores();
        crearEventos();
        crearInscripcionesYPagos();
        crearTiendas();
        crearProductos();
        crearMisEventos();
        crearPqrs();

        long endTime = System.currentTimeMillis();
        double durationSeconds = (endTime - startTime) / 1000.0;

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   ✅ DATOS DE PRUEBA CARGADOS EXITOSAMENTE               ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println(String.format("║   ⏱️  Tiempo de ejecución: %.2f segundos                 ║", durationSeconds));
        System.out.println("║                                                            ║");
        System.out.println("║   📊 RESUMEN:                                              ║");
        System.out.println(String.format("║   • Usuarios: %d (2 admins, 10 proveedores, 7 consumidores) ║", administradores.size() + proveedores.size() + consumidores.size()));
        System.out.println(String.format("║   • Patrocinadores: %d (5 activos, 2 inactivos)            ║", patrocinadores.size()));
        System.out.println(String.format("║   • Eventos: %d (varios estados)                           ║", eventos.size()));
        System.out.println(String.format("║   • Tiendas: %d                                             ║", tiendas.size()));
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
    }

    // ==================== FASE 1: CREAR ROLES ====================
    private void crearRoles() {
        System.out.println("🔐 [1/9] Creando roles del sistema...");

        crearRolSiNoExiste(NombreRol.ADMINISTRADOR);
        crearRolSiNoExiste(NombreRol.PROVEEDOR);
        crearRolSiNoExiste(NombreRol.CONSUMIDOR);

        System.out.println("    ✓ 3 roles creados\n");
    }

    private void crearRolSiNoExiste(NombreRol nombreRol) {
        if (rolRepository.findByNombreRol(nombreRol) == null) {
            Rol rol = new Rol();
            rol.setNombre_rol(nombreRol);
            rolRepository.save(rol);
        }
    }

    // ==================== FASE 2: CREAR USUARIOS ====================
    private void crearUsuarios() {
        System.out.println("👥 [2/9] Creando usuarios del sistema...");

        // 2 Administradores
        administradores.add(crearUsuario(
            "admin@campolibre.com", "admin123", "Carlos Rodríguez",
            "1000000001", "3101234567", NombreRol.ADMINISTRADOR
        ));
        administradores.add(crearUsuario(
            "admin2@campolibre.com", "admin123", "María González",
            "1000000002", "3101234568", NombreRol.ADMINISTRADOR
        ));

        // 10 Proveedores (nombres colombianos realistas)
        proveedores.add(crearUsuario(
            "proveedor1@campolibre.com", "proveedor123", "Juan Pablo Martínez",
            "2000000001", "3201234567", NombreRol.PROVEEDOR
        ));
        proveedores.add(crearUsuario(
            "proveedor2@campolibre.com", "proveedor123", "Ana María López",
            "2000000002", "3201234568", NombreRol.PROVEEDOR
        ));
        proveedores.add(crearUsuario(
            "proveedor3@campolibre.com", "proveedor123", "Pedro Sánchez",
            "2000000003", "3201234569", NombreRol.PROVEEDOR
        ));
        proveedores.add(crearUsuario(
            "proveedor4@campolibre.com", "proveedor123", "Luisa Fernanda Gómez",
            "2000000004", "3201234570", NombreRol.PROVEEDOR
        ));
        proveedores.add(crearUsuario(
            "proveedor5@campolibre.com", "proveedor123", "Andrés Felipe Torres",
            "2000000005", "3201234571", NombreRol.PROVEEDOR
        ));
        proveedores.add(crearUsuario(
            "proveedor6@campolibre.com", "proveedor123", "Carolina Ramírez",
            "2000000006", "3201234572", NombreRol.PROVEEDOR
        ));
        proveedores.add(crearUsuario(
            "proveedor7@campolibre.com", "proveedor123", "Jorge Enrique Díaz",
            "2000000007", "3201234573", NombreRol.PROVEEDOR
        ));
        proveedores.add(crearUsuario(
            "proveedor8@campolibre.com", "proveedor123", "Sandra Milena Castro",
            "2000000008", "3201234574", NombreRol.PROVEEDOR
        ));
        proveedores.add(crearUsuario(
            "proveedor9@campolibre.com", "proveedor123", "Daniel Alberto Morales",
            "2000000009", "3201234575", NombreRol.PROVEEDOR
        ));
        proveedores.add(crearUsuario(
            "proveedor10@campolibre.com", "proveedor123", "Gloria Patricia Vargas",
            "2000000010", "3201234576", NombreRol.PROVEEDOR
        ));

        // 7 Consumidores
        consumidores.add(crearUsuario(
            "consumidor1@campolibre.com", "consumidor123", "Laura Daniela Ruiz",
            "3000000001", "3301234567", NombreRol.CONSUMIDOR
        ));
        consumidores.add(crearUsuario(
            "consumidor2@campolibre.com", "consumidor123", "Miguel Ángel Hernández",
            "3000000002", "3301234568", NombreRol.CONSUMIDOR
        ));
        consumidores.add(crearUsuario(
            "consumidor3@campolibre.com", "consumidor123", "Valentina Pérez",
            "3000000003", "3301234569", NombreRol.CONSUMIDOR
        ));
        consumidores.add(crearUsuario(
            "consumidor4@campolibre.com", "consumidor123", "Santiago Gutiérrez",
            "3000000004", "3301234570", NombreRol.CONSUMIDOR
        ));
        consumidores.add(crearUsuario(
            "consumidor5@campolibre.com", "consumidor123", "Camila Andrea Rojas",
            "3000000005", "3301234571", NombreRol.CONSUMIDOR
        ));
        consumidores.add(crearUsuario(
            "consumidor6@campolibre.com", "consumidor123", "David Alejandro Ortiz",
            "3000000006", "3301234572", NombreRol.CONSUMIDOR
        ));
        consumidores.add(crearUsuario(
            "consumidor7@campolibre.com", "consumidor123", "Isabella Marcela Silva",
            "3000000007", "3301234573", NombreRol.CONSUMIDOR
        ));

        System.out.println("    ✓ " + (administradores.size() + proveedores.size() + consumidores.size()) + " usuarios creados");
        System.out.println("      - " + administradores.size() + " administradores");
        System.out.println("      - " + proveedores.size() + " proveedores");
        System.out.println("      - " + consumidores.size() + " consumidores\n");
    }

    private Usuario crearUsuario(String email, String password, String nombre,
                                 String documento, String telefono, NombreRol nombreRol) {
        Rol rol = rolRepository.findByNombreRol(nombreRol);

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setContrasena(passwordEncoder.encode(password));
        usuario.setNombre(nombre);
        usuario.setDocumento(documento);
        usuario.setTipo_documento(TipoDocumento.CC);
        usuario.setTelefono(telefono);
        usuario.setRol(rol);

        return usuarioRepository.save(usuario);
    }

    // ==================== FASE 3: CREAR PATROCINADORES ====================
    private void crearPatrocinadores() {
        System.out.println("🏢 [3/9] Creando patrocinadores...");

        // 5 Patrocinadores ACTIVOS
        patrocinadores.add(crearPatrocinador(
            "Alcaldía de Bogotá",
            "Gobierno distrital comprometido con el desarrollo rural y el fortalecimiento del sector agrícola.",
            "eventos@bogota.gov.co", "6013649090", "https://bogota.gov.co",
            "https://ejemplo.com/logos/alcaldia.png", true
        ));

        patrocinadores.add(crearPatrocinador(
            "Ministerio de Agricultura y Desarrollo Rural",
            "Entidad rectora de las políticas agropecuarias en Colombia.",
            "atencion@minagricultura.gov.co", "6013349800", "https://www.minagricultura.gov.co",
            "https://ejemplo.com/logos/minagricultura.png", true
        ));

        patrocinadores.add(crearPatrocinador(
            "Bancóldex",
            "Banco de desarrollo empresarial de Colombia, apoyando el sector agrícola.",
            "contacto@bancoldex.com", "6013532700", "https://www.bancoldex.com",
            "https://ejemplo.com/logos/bancoldex.png", true
        ));

        patrocinadores.add(crearPatrocinador(
            "Fedepanela",
            "Federación Colombiana de Productores de Panela.",
            "info@fedepanela.org.co", "6012328484", "https://www.fedepanela.org.co",
            "https://ejemplo.com/logos/fedepanela.png", true
        ));

        patrocinadores.add(crearPatrocinador(
            "ProColombia",
            "Entidad que promueve las exportaciones y el turismo internacional.",
            "info@procolombia.co", "6015876000", "https://www.procolombia.co",
            "https://ejemplo.com/logos/procolombia.png", true
        ));

        // 2 Patrocinadores INACTIVOS (para probar validaciones)
        patrocinadores.add(crearPatrocinador(
            "Agro Export SA (Archivado)",
            "Empresa archivada por fusión con otra compañía.",
            "contacto@agroexport.com", "6011234567", "https://www.agroexport.com",
            "https://ejemplo.com/logos/agroexport.png", false
        ));

        patrocinadores.add(crearPatrocinador(
            "Fondo Campesino (Inactivo)",
            "Fondo cerrado temporalmente por reestructuración.",
            "info@fondocampesino.org", "6017654321", "https://www.fondocampesino.org",
            "https://ejemplo.com/logos/fondocampesino.png", false
        ));

        System.out.println("    ✓ " + patrocinadores.size() + " patrocinadores creados");
        System.out.println("      - " + patrocinadores.stream().filter(Patrocinador::getActivo).count() + " activos");
        System.out.println("      - " + patrocinadores.stream().filter(p -> !p.getActivo()).count() + " inactivos\n");
    }

    private Patrocinador crearPatrocinador(String nombre, String descripcion, String email,
                                           String telefono, String sitioWeb, String logoUrl, boolean activo) {
        Patrocinador p = new Patrocinador();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setContactoEmail(email);
        p.setTelefonoContacto(telefono);
        p.setSitioWeb(sitioWeb);
        p.setLogoUrl(logoUrl);
        p.setActivo(activo);
        return patrocinadorRepository.save(p);
    }

    // ==================== FASE 4: CREAR EVENTOS ====================
    private void crearEventos() {
        System.out.println("📅 [4/9] Creando eventos...");

        Usuario admin = administradores.get(0);

        // Obtener patrocinadores activos
        Patrocinador pat1 = patrocinadores.get(0); // Alcaldía
        Patrocinador pat2 = patrocinadores.get(1); // MinAgricultura
        Patrocinador pat3 = patrocinadores.get(2); // Bancóldex
        Patrocinador pat4 = patrocinadores.get(3); // Fedepanela
        Patrocinador pat5 = patrocinadores.get(4); // ProColombia

        // ========== 2 EVENTOS BORRADOR ==========
        eventos.add(crearEvento(
            "Feria de la Panela y Derivados",
            "Feria dedicada exclusivamente a los productores de panela, mieles y derivados.",
            "Coliseo Municipal de Villeta", "Calle 5 # 6-40, Centro, Villeta, Cundinamarca", "Villeta",
            LocalDate.now().plusDays(35), LocalTime.of(9, 0),
            TipoEvento.FERIA, EstadoEvento.BORRADOR, null,
            admin, pat4, 35, 60000.0,
            "https://viajaporcolombia.com/images/turistico-y-reinado-nacional-de-la-panela-2024-en-villeta-cundinamarca.jpg",
            "Términos en revisión. Pendiente de publicación."
        ));

        eventos.add(crearEvento(
            "Congreso Nacional de Agricultura Sostenible",
            "Encuentro de profesionales del agro para discutir sostenibilidad y nuevas tecnologías.",
            "Centro de Convenciones Gonzalo Jiménez de Quesada", "Calle 24 # 5-60, Bogotá D.C.", "Bogotá",
            LocalDate.now().plusDays(40), LocalTime.of(8, 0),
            TipoEvento.CAPACITACION, EstadoEvento.BORRADOR, null,
            admin, pat2, 100, 150000.0,
            "https://ejemplo.com/congreso.jpg",
            "Evento en planificación. Pendiente confirmación de conferencistas."
        ));

        // ========== 5 EVENTOS PUBLICADOS ==========
        // Evento con cupos casi llenos
        eventos.add(crearEvento(
            "Feria Agrícola Regional 2025",
            "Gran feria donde productores del campo mostrarán sus mejores productos.",
            "Parque Principal de Fusagasugá", "Carrera 7 # 5-40, Fusagasugá, Cundinamarca", "Fusagasugá",
            LocalDate.now().plusDays(15), LocalTime.of(9, 0),
            TipoEvento.FERIA, EstadoEvento.PUBLICADO, LocalDateTime.now().minusDays(5),
            admin, pat1, 50, 80000.0,
            "https://elgoibar.eus/wp-content/uploads/2024/05/azoka2.jpg",
            "1. Espacio asignado de 3x3m con mesa y silla.\n2. Llegada 2 horas antes.\n3. Mantener limpieza."
        ));

        // Evento con cupos disponibles
        eventos.add(crearEvento(
            "Taller de Agricultura Orgánica y Sostenible",
            "Aprende técnicas modernas de cultivo orgánico y manejo sostenible.",
            "Auditorio SENA, Centro Agroindustrial", "Km 7 Vía Fusagasugá - Arbeláez", "Fusagasugá",
            LocalDate.now().plusDays(7), LocalTime.of(14, 0),
            TipoEvento.TALLER, EstadoEvento.PUBLICADO, LocalDateTime.now().minusDays(10),
            admin, pat2, 30, 35000.0,
            "https://d3puay5pkxu9s4.cloudfront.net/curso/545/800_imagen.jpg",
            "1. Taller de 4 horas. 2. Incluye certificado y kit de semillas."
        ));

        eventos.add(crearEvento(
            "Festival del Café y el Cacao Colombiano",
            "Celebración de dos de los productos más emblemáticos de Colombia.",
            "Centro de Convenciones Compensar", "Avenida Carrera 68 # 49A-47, Bogotá D.C.", "Bogotá",
            LocalDate.now().plusDays(22), LocalTime.of(10, 0),
            TipoEvento.FESTIVAL, EstadoEvento.PUBLICADO, LocalDateTime.now().minusDays(3),
            admin, pat5, 40, 120000.0,
            "https://www.coffeemedia.com.co/wp-content/uploads/2024/03/Feria-Internacional-de-Cafe-Cacao-y-Agroturismo-FICCA.png",
            "1. Stand equipado incluido. 2. Registro sanitario INVIMA requerido."
        ));

        // Evento con fecha próxima (para probar reembolsos)
        eventos.add(crearEvento(
            "Mercado Agroecológico de Bogotá",
            "Mercado especializado en productos 100% orgánicos certificados.",
            "Parque El Virrey", "Calle 88 entre Carreras 11 y 15, Bogotá D.C.", "Bogotá",
            LocalDate.now().plusDays(3), LocalTime.of(8, 0),
            TipoEvento.MERCADO, EstadoEvento.PUBLICADO, LocalDateTime.now().minusDays(12),
            admin, pat1, 30, 45000.0,
            "https://desarrolloeconomico.gov.co/wp-content/uploads/2025/10/01032025-_W5A5223MERCADOS-CAMPESINOS-JARDIN-BOTANICO-s.jpg",
            "1. Mercado de 8:00 AM a 1:00 PM. 2. Solo productos certificados orgánicos."
        ));

        eventos.add(crearEvento(
            "Capacitación en Transformación de Lácteos",
            "Curso intensivo sobre elaboración artesanal de quesos y derivados.",
            "Instituto Técnico Agrícola - La Mesa", "Vereda San Javier, Km 2, La Mesa, Cundinamarca", "La Mesa",
            LocalDate.now().plusDays(12), LocalTime.of(8, 0),
            TipoEvento.CAPACITACION, EstadoEvento.PUBLICADO, LocalDateTime.now().minusDays(8),
            admin, pat4, 20, 50000.0,
            "https://cajica.gov.co/wp-content/uploads/2021/09/241324563_4360624100670269_4875759541628949259_n.jpg",
            "1. Día completo 8:00 AM - 5:00 PM. 2. Incluye almuerzo y certificado."
        ));

        // ========== 2 EVENTOS EN_CURSO ==========
        eventos.add(crearEvento(
            "Mercado Campesino de Fin de Semana",
            "Mercado campesino semanal con productos frescos directamente del productor.",
            "Plaza de Mercado Municipal - Soacha", "Calle 13 # 6-20, Centro, Soacha", "Soacha",
            LocalDate.now(), LocalTime.of(7, 0),
            TipoEvento.MERCADO, EstadoEvento.EN_CURSO, LocalDateTime.now().minusDays(7),
            admin, pat1, 25, 25000.0,
            "https://bogota.gov.co/sites/default/files/u3884/mercados1.jpg",
            "1. Mercado de 7:00 AM a 2:00 PM. 2. Espacio 2x2m sin mobiliario."
        ));

        eventos.add(crearEvento(
            "Expo Agro 2025 - Innovación Tecnológica",
            "Exposición de tecnología e innovación para el sector agrícola.",
            "Plaza Mayor Medellín", "Calle 41 # 55-80, Medellín, Antioquia", "Medellín",
            LocalDate.now(), LocalTime.of(10, 0),
            TipoEvento.EXPOSICION, EstadoEvento.EN_CURSO, LocalDateTime.now().minusDays(14),
            admin, pat3, 60, 200000.0,
            "https://ejemplo.com/expo-agro.jpg",
            "1. Stand de 9m² con paredes y alfombra. 2. Conexión eléctrica incluida."
        ));

        // ========== 2 EVENTOS FINALIZADOS ==========
        eventos.add(crearEvento(
            "Exposición Nacional de Maquinaria Agrícola",
            "Exposición de las últimas tecnologías en maquinaria para el agro.",
            "Corferias - Pabellón 3", "Carrera 37 # 24-67, Bogotá D.C.", "Bogotá",
            LocalDate.now().minusDays(10), LocalTime.of(9, 0),
            TipoEvento.EXPOSICION, EstadoEvento.FINALIZADO, LocalDateTime.now().minusDays(30),
            admin, pat3, 80, 350000.0,
            "https://www.clarin.com/img/2022/07/27/ol6Sxmm7S_2000x1500__1.jpg",
            "1. Stand de 9m². 2. Montaje el día anterior."
        ));

        eventos.add(crearEvento(
            "Festival Gastronómico del Campo Colombiano",
            "Festival que celebró la riqueza culinaria del campo colombiano.",
            "Parque Simón Bolívar", "Calle 63 # 50-00, Bogotá D.C.", "Bogotá",
            LocalDate.now().minusDays(25), LocalTime.of(11, 0),
            TipoEvento.FESTIVAL, EstadoEvento.FINALIZADO, LocalDateTime.now().minusDays(45),
            admin, pat5, 55, 95000.0,
            "https://www.sena.edu.co/es-co/Noticias/NoticiasImg/Guajira3-19102023.jpg",
            "1. Stand 4x4m con techo. 2. Curso manipulación alimentos requerido."
        ));

        // ========== 1 EVENTO CANCELADO ==========
        eventos.add(crearEvento(
            "Taller de Apicultura para Principiantes",
            "EVENTO CANCELADO. Curso básico sobre manejo de colmenas.",
            "Finca Demostrativa El Colmenar", "Vereda El Triunfo, Km 8, Silvania", "Silvania",
            LocalDate.now().plusDays(5), LocalTime.of(9, 0),
            TipoEvento.TALLER, EstadoEvento.CANCELADO, LocalDateTime.now().minusDays(15),
            admin, pat2, 15, 40000.0,
            "https://escueladeabejas.com/wp-content/uploads/2025/06/taller-subproductos-apicolas-escuela-de-abejas.png",
            "EVENTO CANCELADO. Reembolso automático a inscritos."
        ));

        System.out.println("    ✓ " + eventos.size() + " eventos creados");
        System.out.println("      - BORRADOR: " + eventos.stream().filter(e -> e.getEstado() == EstadoEvento.BORRADOR).count());
        System.out.println("      - PUBLICADO: " + eventos.stream().filter(e -> e.getEstado() == EstadoEvento.PUBLICADO).count());
        System.out.println("      - EN_CURSO: " + eventos.stream().filter(e -> e.getEstado() == EstadoEvento.EN_CURSO).count());
        System.out.println("      - FINALIZADO: " + eventos.stream().filter(e -> e.getEstado() == EstadoEvento.FINALIZADO).count());
        System.out.println("      - CANCELADO: " + eventos.stream().filter(e -> e.getEstado() == EstadoEvento.CANCELADO).count() + "\n");
    }

    private Evento crearEvento(String nombre, String descripcion, String ubicacion, String direccion,
                               String ciudad, LocalDate fecha, LocalTime hora, TipoEvento tipo,
                               EstadoEvento estado, LocalDateTime fechaPublicacion, Usuario creador,
                               Patrocinador patrocinador, Integer cuposMax, Double costo,
                               String imagen, String terminos) {
        Evento e = new Evento();
        e.setNombre(nombre);
        e.setDescripcion(descripcion);
        e.setUbicacion(ubicacion);
        e.setDireccionCompleta(direccion);
        e.setCiudad(ciudad);
        e.setFecha_evento(fecha);
        e.setHora_evento(hora);
        e.setTipo_evento(tipo);
        e.setEstado(estado);
        e.setFechaPublicacion(fechaPublicacion);
        e.setCreado_por(creador);
        e.setPatrocinador(patrocinador);
        e.setCuposMaximosProveedor(cuposMax);
        e.setCuposOcupados(0); // Se actualizará con inscripciones
        e.setCostoEspacio(costo);
        e.setImagen_evento(imagen);
        e.setTerminosCondiciones(terminos);
        return eventoRepository.save(e);
    }

    // ==================== FASE 5: CREAR INSCRIPCIONES Y PAGOS ====================
    private void crearInscripcionesYPagos() {
        System.out.println("👥 [5/9] Creando inscripciones y pagos...");

        int inscripcionesCreadas = 0;

        // Obtener eventos para inscripciones
        Evento e1 = eventos.get(2); // Feria Agrícola - PUBLICADO
        Evento e2 = eventos.get(3); // Taller Agricultura - PUBLICADO
        Evento e3 = eventos.get(4); // Festival Café - PUBLICADO
        Evento e4 = eventos.get(5); // Mercado Agroecológico - PUBLICADO
        Evento e5 = eventos.get(6); // Capacitación Lácteos - PUBLICADO
        Evento e6 = eventos.get(7); // Mercado Campesino - EN_CURSO
        Evento e7 = eventos.get(8); // Expo Agro - EN_CURSO
        Evento e8 = eventos.get(9); // Exposición Maquinaria - FINALIZADO
        Evento e9 = eventos.get(10); // Festival Gastronómico - FINALIZADO

        // ========== EVENTO 1: Feria Agrícola (llenar 40% cupos) ==========
        inscripcionesCreadas += crearInscripcion(proveedores.get(0), e1, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.PSE);
        inscripcionesCreadas += crearInscripcion(proveedores.get(1), e1, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_CREDITO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(2), e1, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_DEBITO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(3), e1, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.EFECTIVO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(4), e1, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.PSE);
        inscripcionesCreadas += crearInscripcion(proveedores.get(5), e1, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_CREDITO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(6), e1, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_DEBITO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(7), e1, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.PSE);
        inscripcionesCreadas += crearInscripcion(proveedores.get(8), e1, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.EFECTIVO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(9), e1, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_CREDITO);
        // 2 inscripciones PENDIENTES
        inscripcionesCreadas += crearInscripcion(proveedores.get(0), e1, EstadoCupo.PENDIENTE, EstadoPago.PENDIENTE, MetodoPago.PSE);
        inscripcionesCreadas += crearInscripcion(proveedores.get(1), e1, EstadoCupo.PENDIENTE, EstadoPago.PENDIENTE, MetodoPago.TARJETA_CREDITO);

        // ========== EVENTO 2: Taller Agricultura ==========
        inscripcionesCreadas += crearInscripcion(proveedores.get(0), e2, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.EFECTIVO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(2), e2, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_CREDITO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(4), e2, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.PSE);
        inscripcionesCreadas += crearInscripcion(proveedores.get(6), e2, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_DEBITO);

        // ========== EVENTO 3: Festival Café ==========
        inscripcionesCreadas += crearInscripcion(proveedores.get(1), e3, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.PSE);
        inscripcionesCreadas += crearInscripcion(proveedores.get(3), e3, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_CREDITO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(5), e3, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.EFECTIVO);
        // 1 inscripción CANCELADA (pago fallido)
        inscripcionesCreadas += crearInscripcionConError(proveedores.get(7), e3, EstadoCupo.CANCELADO, EstadoPago.FALLIDO,
            MetodoPago.TARJETA_CREDITO, "Tarjeta declinada - Fondos insuficientes");

        // ========== EVENTO 4: Mercado Agroecológico (fecha próxima) ==========
        inscripcionesCreadas += crearInscripcion(proveedores.get(2), e4, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_DEBITO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(4), e4, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.PSE);
        inscripcionesCreadas += crearInscripcion(proveedores.get(6), e4, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.EFECTIVO);
        // 1 inscripción PENDIENTE
        inscripcionesCreadas += crearInscripcion(proveedores.get(8), e4, EstadoCupo.PENDIENTE, EstadoPago.PENDIENTE, MetodoPago.PSE);

        // ========== EVENTO 5: Capacitación Lácteos ==========
        inscripcionesCreadas += crearInscripcion(proveedores.get(1), e5, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_CREDITO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(3), e5, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.PSE);
        // 1 pago FALLIDO
        inscripcionesCreadas += crearInscripcionConError(proveedores.get(5), e5, EstadoCupo.CANCELADO, EstadoPago.FALLIDO,
            MetodoPago.TARJETA_DEBITO, "Error de comunicación con banco emisor");

        // ========== EVENTO 6: Mercado Campesino (EN_CURSO) ==========
        inscripcionesCreadas += crearInscripcion(proveedores.get(0), e6, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.EFECTIVO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(2), e6, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.PSE);
        inscripcionesCreadas += crearInscripcion(proveedores.get(4), e6, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_CREDITO);

        // ========== EVENTO 7: Expo Agro (EN_CURSO) ==========
        inscripcionesCreadas += crearInscripcion(proveedores.get(1), e7, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_DEBITO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(3), e7, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.PSE);

        // ========== EVENTO 8: Exposición Maquinaria (FINALIZADO) ==========
        inscripcionesCreadas += crearInscripcion(proveedores.get(0), e8, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_CREDITO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(1), e8, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.PSE);
        inscripcionesCreadas += crearInscripcion(proveedores.get(2), e8, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.EFECTIVO);

        // ========== EVENTO 9: Festival Gastronómico (FINALIZADO) ==========
        inscripcionesCreadas += crearInscripcion(proveedores.get(3), e9, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.PSE);
        inscripcionesCreadas += crearInscripcion(proveedores.get(4), e9, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_CREDITO);
        inscripcionesCreadas += crearInscripcion(proveedores.get(5), e9, EstadoCupo.CONFIRMADO, EstadoPago.EXITOSO, MetodoPago.TARJETA_DEBITO);

        // Contar estados
        long confirmadas = inscripcionProveedorRepository.findAll().stream().filter(i -> i.getEstadoCupo() == EstadoCupo.CONFIRMADO).count();
        long pendientes = inscripcionProveedorRepository.findAll().stream().filter(i -> i.getEstadoCupo() == EstadoCupo.PENDIENTE).count();
        long canceladas = inscripcionProveedorRepository.findAll().stream().filter(i -> i.getEstadoCupo() == EstadoCupo.CANCELADO).count();

        System.out.println("    ✓ " + inscripcionesCreadas + " inscripciones y pagos creados");
        System.out.println("      - CONFIRMADAS: " + confirmadas);
        System.out.println("      - PENDIENTES: " + pendientes);
        System.out.println("      - CANCELADAS: " + canceladas + "\n");

        // Verificar consistencia
        verificarConsistenciaCupos();
    }

    private int crearInscripcion(Usuario proveedor, Evento evento, EstadoCupo estadoCupo,
                                 EstadoPago estadoPago, MetodoPago metodoPago) {
        return crearInscripcionConError(proveedor, evento, estadoCupo, estadoPago, metodoPago, null);
    }

    private int crearInscripcionConError(Usuario proveedor, Evento evento, EstadoCupo estadoCupo,
                                         EstadoPago estadoPago, MetodoPago metodoPago, String mensajeError) {
        try {
            // 1. Crear inscripción
            InscripcionProveedor inscripcion = new InscripcionProveedor();
            inscripcion.setProveedor(proveedor);
            inscripcion.setEvento(evento);
            inscripcion.setEstadoCupo(estadoCupo);
            inscripcion.setCostoPagado(evento.getCostoEspacio());

            if (estadoCupo == EstadoCupo.CONFIRMADO) {
                inscripcion.setFechaConfirmacion(LocalDateTime.now().minusDays(random.nextInt(10) + 1));
            }

            InscripcionProveedor inscripcionGuardada = inscripcionProveedorRepository.save(inscripcion);

            // 2. Crear pago
            PagoEvento pago = new PagoEvento();
            pago.setInscripcionProveedor(inscripcionGuardada);
            pago.setMonto(evento.getCostoEspacio());
            pago.setMetodoPago(metodoPago);
            pago.setEstado(estadoPago);

            if (estadoPago == EstadoPago.FALLIDO && mensajeError != null) {
                pago.setMensajeError(mensajeError);
            }

            // Agregar detalles específicos según método de pago
            if (metodoPago == MetodoPago.PSE) {
                pago.setEntidadBancaria(random.nextBoolean() ? EntidadBancaria.NEQUI : EntidadBancaria.DAVIPLATA);
                pago.setTipoPersonaPSE(TipoPersonaPSE.NATURAL);
                pago.setNumeroDocumentoPSE(proveedor.getDocumento());
            } else if (metodoPago == MetodoPago.TARJETA_CREDITO || metodoPago == MetodoPago.TARJETA_DEBITO) {
                EntidadBancaria[] bancosConTarjeta = {EntidadBancaria.BANCOLOMBIA, EntidadBancaria.DAVIVIENDA, EntidadBancaria.BBVA};
                pago.setEntidadBancaria(bancosConTarjeta[random.nextInt(bancosConTarjeta.length)]);
            }

            PagoEvento pagoGuardado = pagoEventoRepository.save(pago);

            // 3. Vincular pago a inscripción
            inscripcionGuardada.setPagoEvento(pagoGuardado);
            inscripcionProveedorRepository.save(inscripcionGuardada);

            // 4. Actualizar cupos si pago exitoso
            if (estadoPago == EstadoPago.EXITOSO) {
                evento.setCuposOcupados(evento.getCuposOcupados() + 1);
                eventoRepository.save(evento);
            }

            return 1;
        } catch (Exception e) {
            System.err.println("⚠️ Error al crear inscripción: " + e.getMessage());
            return 0;
        }
    }

    private void verificarConsistenciaCupos() {
        System.out.println("    🔍 Verificando consistencia de cupos...");

        boolean hayInconsistencias = false;
        for (Evento evento : eventos) {
            Long cuposConfirmados = inscripcionProveedorRepository
                .countByEventoIdAndEstadoCupo(evento.getId_evento(), EstadoCupo.CONFIRMADO);

            if (!cuposConfirmados.equals((long) evento.getCuposOcupados())) {
                System.err.println("      ⚠️ INCONSISTENCIA: " + evento.getNombre());
                System.err.println("         Cupos evento: " + evento.getCuposOcupados() + " | Inscripciones: " + cuposConfirmados);
                hayInconsistencias = true;
            }
        }

        if (!hayInconsistencias) {
            System.out.println("      ✓ Cupos consistentes en todos los eventos");
        }
    }

    // ==================== CONTINUARÁ EN SIGUIENTE PARTE... ====================
    // Debido al límite de caracteres, continuaré con las demás fases en métodos separados

    // ==================== FASE 6: CREAR TIENDAS ====================
    private void crearTiendas() {
        System.out.println("🏪 [6/9] Creando tiendas...");

        // Distribuir tiendas entre proveedores (cada proveedor tiene 1-2 tiendas)
        tiendas.add(crearTienda("El Huerto Fresco", "Frutas y verduras frescas directamente del campo",
            CategoriaTienda.FRUTAS_VERDURAS, "huerto@campolibre.com", "3101234567",
            "Fusagasugá, Cundinamarca", proveedores.get(0), EstadoTienda.ACTIVA));

        tiendas.add(crearTienda("Delicias del Valle", "Productos artesanales con ingredientes naturales",
            CategoriaTienda.PROCESADOS_ARTESANALES, "delicias@campolibre.com", "3102345678",
            "Cali, Valle del Cauca", proveedores.get(1), EstadoTienda.ACTIVA));

        tiendas.add(crearTienda("Semillas de Oro", "Granos, cereales y semillas de calidad",
            CategoriaTienda.GRANOS_SEMILLAS, "semillas@campolibre.com", "3103456789",
            "Bogotá, Cundinamarca", proveedores.get(2), EstadoTienda.ACTIVA));

        tiendas.add(crearTienda("Aromas del Campo", "Hierbas aromáticas y especias naturales",
            CategoriaTienda.HIERBAS_ESPECIAS, "aromas@campolibre.com", "3104567890",
            "Pereira, Risaralda", proveedores.get(3), EstadoTienda.ACTIVA));

        tiendas.add(crearTienda("Lácteos La Pradera", "Productos lácteos artesanales del campo",
            CategoriaTienda.PROCESADOS_ARTESANALES, "lacteos@campolibre.com", "3105678901",
            "Medellín, Antioquia", proveedores.get(4), EstadoTienda.ACTIVA));

        // Proveedor 5 con 2 tiendas
        tiendas.add(crearTienda("Café Premium del Eje", "Café especial del Eje Cafetero",
            CategoriaTienda.FRUTAS_VERDURAS, "cafe@campolibre.com", "3106789012",
            "Armenia, Quindío", proveedores.get(5), EstadoTienda.ACTIVA));

        tiendas.add(crearTienda("Panela del Trapiche", "Panela y derivados de la caña",
            CategoriaTienda.PROCESADOS_ARTESANALES, "panela@campolibre.com", "3107890123",
            "Villeta, Cundinamarca", proveedores.get(5), EstadoTienda.ACTIVA));

        // Proveedor 6 con 2 tiendas
        tiendas.add(crearTienda("Hortalizas Frescas", "Verduras y hortalizas de la región",
            CategoriaTienda.FRUTAS_VERDURAS, "hortalizas@campolibre.com", "3108901234",
            "Soacha, Cundinamarca", proveedores.get(6), EstadoTienda.ACTIVA));

        tiendas.add(crearTienda("Granja Orgánica El Sol", "Productos 100% orgánicos certificados",
            CategoriaTienda.FRUTAS_VERDURAS, "granja@campolibre.com", "3109012345",
            "Chía, Cundinamarca", proveedores.get(6), EstadoTienda.ACTIVA));

        // 1 tienda INACTIVA (para pruebas)
        tiendas.add(crearTienda("Tienda Temporal (Inactiva)", "Tienda temporalmente cerrada",
            CategoriaTienda.GRANOS_SEMILLAS, "temporal@campolibre.com", "3100000000",
            "Bogotá", proveedores.get(7), EstadoTienda.INACTIVA));

        System.out.println("    ✓ " + tiendas.size() + " tiendas creadas");
        System.out.println("      - ACTIVAS: " + tiendas.stream().filter(t -> t.getEstado() == EstadoTienda.ACTIVA).count());
        System.out.println("      - INACTIVAS: " + tiendas.stream().filter(t -> t.getEstado() == EstadoTienda.INACTIVA).count() + "\n");
    }

    private Tienda crearTienda(String nombre, String descripcion, CategoriaTienda categoria,
                               String email, String telefono, String ubicacion,
                               Usuario propietario, EstadoTienda estado) {
        Tienda t = new Tienda();
        t.setNombre(nombre);
        t.setDescripcion(descripcion);
        t.setCategoriaPrincipal(categoria);
        t.setCorreo_tienda(email);
        t.setTelefono_tienda(telefono);
        t.setUbicacion(ubicacion);
        t.setUsuario(propietario);
        t.setEstado(estado);
        return tiendaRepository.save(t);
    }

    // ==================== FASE 7: CREAR PRODUCTOS ====================
    private void crearProductos() {
        System.out.println("📦 [7/9] Creando productos...");

        int productosCreados = 0;

        // Productos por tienda (4-5 productos cada una)
        for (Tienda tienda : tiendas) {
            if (tienda.getEstado() == EstadoTienda.ACTIVA) {
                switch (tienda.getCategoriaPrincipal()) {
                    case FRUTAS_VERDURAS:
                        productosCreados += crearProductosFrutasVerduras(tienda);
                        break;
                    case PROCESADOS_ARTESANALES:
                        productosCreados += crearProductosProcesados(tienda);
                        break;
                    case GRANOS_SEMILLAS:
                        productosCreados += crearProductosGranos(tienda);
                        break;
                    case HIERBAS_ESPECIAS:
                        productosCreados += crearProductosHierbas(tienda);
                        break;
                }
            }
        }

        System.out.println("    ✓ " + productosCreados + " productos creados\n");
    }

    private int crearProductosFrutasVerduras(Tienda tienda) {
        int count = 0;
        count += crearProducto("Mango Tommy", "Mangos frescos y dulces", 5000.0, 50, 10,
            SubcategoriaProducto.FRUTAS_TROPICALES, UnidadMedida.KILO, tienda);
        count += crearProducto("Plátano Hartón", "Plátanos verdes para cocinar", 2500.0, 100, 15,
            SubcategoriaProducto.FRUTAS_TROPICALES, UnidadMedida.LIBRA, tienda);
        count += crearProducto("Naranja Valencia", "Naranjas jugosas ideales para jugo", 3000.0, 80, 10,
            SubcategoriaProducto.FRUTAS_CITRICAS, UnidadMedida.KILO, tienda);
        count += crearProducto("Lechuga Crespa", "Lechuga fresca y crujiente", 2000.0, 30, 5,
            SubcategoriaProducto.VERDURAS_HOJA, UnidadMedida.UNIDAD, tienda);
        count += crearProducto("Papa Criolla", "Papa criolla para freír", 4000.0, 60, 10,
            SubcategoriaProducto.TUBERCULOS, UnidadMedida.LIBRA, tienda);
        return count;
    }

    private int crearProductosProcesados(Tienda tienda) {
        int count = 0;
        count += crearProducto("Mermelada de Mora", "Mermelada artesanal sin conservantes", 8000.0, 25, 5,
            SubcategoriaProducto.MERMELADAS, UnidadMedida.UNIDAD, tienda);
        count += crearProducto("Queso Campesino", "Queso fresco hecho en casa", 12000.0, 15, 3,
            SubcategoriaProducto.QUESOS, UnidadMedida.LIBRA, tienda);
        count += crearProducto("Pan Integral", "Pan artesanal con semillas", 6000.0, 20, 5,
            SubcategoriaProducto.PAN_ARTESANAL, UnidadMedida.UNIDAD, tienda);
        count += crearProducto("Salsa de Tomate Casera", "Salsa natural sin químicos", 7000.0, 30, 5,
            SubcategoriaProducto.SALSAS, UnidadMedida.UNIDAD, tienda);
        return count;
    }

    private int crearProductosGranos(Tienda tienda) {
        int count = 0;
        count += crearProducto("Fríjol Rojo", "Fríjol de excelente calidad", 6000.0, 50, 10,
            SubcategoriaProducto.GRANOS_SECOS, UnidadMedida.LIBRA, tienda);
        count += crearProducto("Arroz Integral", "Arroz sin procesar", 5500.0, 60, 10,
            SubcategoriaProducto.CEREALES, UnidadMedida.KILO, tienda);
        count += crearProducto("Semillas de Chía", "Semillas nutritivas", 15000.0, 20, 5,
            SubcategoriaProducto.SEMILLAS, UnidadMedida.BOLSA_1KG, tienda);
        count += crearProducto("Lentejas", "Lentejas de grano grande", 7000.0, 35, 8,
            SubcategoriaProducto.LEGUMINOSAS, UnidadMedida.LIBRA, tienda);
        return count;
    }

    private int crearProductosHierbas(Tienda tienda) {
        int count = 0;
        count += crearProducto("Albahaca Fresca", "Albahaca aromática", 3000.0, 15, 3,
            SubcategoriaProducto.HIERBAS_AROMATICAS, UnidadMedida.UNIDAD, tienda);
        count += crearProducto("Canela en Rama", "Canela de alta calidad", 12000.0, 10, 2,
            SubcategoriaProducto.ESPECIAS_SECAS, UnidadMedida.PAQUETE, tienda);
        count += crearProducto("Té de Hierbabuena", "Infusión digestiva", 5000.0, 30, 5,
            SubcategoriaProducto.INFUSIONES_TES, UnidadMedida.PAQUETE, tienda);
        count += crearProducto("Romero Seco", "Romero para condimentar", 4000.0, 20, 4,
            SubcategoriaProducto.ESPECIAS_SECAS, UnidadMedida.PAQUETE, tienda);
        return count;
    }

    private int crearProducto(String nombre, String descripcion, Double precio, Integer stock,
                              Integer stockMin, SubcategoriaProducto subcat, UnidadMedida unidad, Tienda tienda) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setStockMinimo(stockMin);
        p.setSubcategoria(subcat);
        p.setUnidadMedida(unidad);
        p.setTienda(tienda);
        p.setEstado("ACTIVO");
        productoRepository.save(p);
        return 1;
    }

    // ==================== FASE 8: CREAR MIS EVENTOS ====================
    private void crearMisEventos() {
        System.out.println("⭐ [8/9] Creando registros Mis Eventos...");

        int misEventosCreados = 0;

        // Solo eventos PUBLICADOS y EN_CURSO pueden ser guardados por consumidores
        List<Evento> eventosDisponibles = eventos.stream()
            .filter(e -> e.getEstado() == EstadoEvento.PUBLICADO || e.getEstado() == EstadoEvento.EN_CURSO)
            .toList();

        // Cada consumidor guarda 2-3 eventos
        for (int i = 0; i < Math.min(consumidores.size(), 5); i++) {
            Usuario consumidor = consumidores.get(i);

            // Guardar 2-3 eventos aleatorios
            int numEventos = 2 + random.nextInt(2); // 2 o 3
            for (int j = 0; j < numEventos && j < eventosDisponibles.size(); j++) {
                Evento evento = eventosDisponibles.get((i + j) % eventosDisponibles.size());
                boolean notificado = random.nextBoolean();

                misEventosCreados += crearMisEvento(consumidor, evento, notificado);
            }
        }

        System.out.println("    ✓ " + misEventosCreados + " registros Mis Eventos creados\n");
    }

    private int crearMisEvento(Usuario consumidor, Evento evento, boolean notificado) {
        try {
            MisEventos me = new MisEventos();
            me.setUsuario(consumidor);
            me.setEvento(evento);
            me.setNotificado(notificado);
            misEventosRepository.save(me);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== FASE 9: CREAR PQRS ====================
    private void crearPqrs() {
        System.out.println("📝 [9/9] Creando PQRS...");

        Usuario admin = administradores.get(0);
        Usuario consumidor1 = consumidores.get(0);
        Usuario consumidor2 = consumidores.get(1);
        Usuario proveedor1 = proveedores.get(0);

        Tienda tienda1 = tiendas.get(0);
        Evento evento1 = eventos.get(2); // Feria Agrícola

        int pqrsCreadas = 0;

        // PQRS 1 - PENDIENTE sobre tienda
        Pqrs pqrs1 = new Pqrs();
        pqrs1.setTipo(TipoPqrs.PREGUNTA);
        pqrs1.setDescripcion("¿Hacen envíos a domicilio? Vivo lejos pero me interesan sus productos.");
        pqrs1.setEstado(EstadoPqrs.PENDIENTE);
        pqrs1.setEmisor(consumidor1);
        pqrs1.setReceptor(admin);
        pqrs1.setPendienteDe(RolProceso.PROVEEDOR);
        pqrsRepository.save(pqrs1);

        PqrsTienda pt1 = new PqrsTienda();
        pt1.setPqrs(pqrs1);
        pt1.setTienda(tienda1);
        pqrsTiendaRepository.save(pt1);
        pqrsCreadas++;

        // PQRS 2 - RESPONDIDA sobre evento
        Pqrs pqrs2 = new Pqrs();
        pqrs2.setTipo(TipoPqrs.PREGUNTA);
        pqrs2.setDescripcion("¿La entrada al evento es gratuita o tiene costo?");
        pqrs2.setEstado(EstadoPqrs.RESPONDIDA);
        pqrs2.setEmisor(consumidor2);
        pqrs2.setReceptor(consumidor2);
        pqrs2.setPendienteDe(RolProceso.CONSUMIDOR);
        pqrsRepository.save(pqrs2);

        PqrsRespuesta resp2 = new PqrsRespuesta();
        resp2.setPqrs(pqrs2);
        resp2.setContenido("La entrada es completamente gratuita para todos los asistentes.");
        resp2.setEmisor(admin);
        resp2.setEmitidoPor(RolProceso.PROVEEDOR);
        resp2.setFechaEmision(LocalDateTime.now().minusDays(1));
        pqrsRespuestaRepository.save(resp2);

        PqrsEvento pe1 = new PqrsEvento();
        pe1.setPqrs(pqrs2);
        pe1.setEvento(evento1);
        pqrsEventoRepository.save(pe1);
        pqrsCreadas++;

        // PQRS 3 - SUGERENCIA RESPONDIDA
        Pqrs pqrs3 = new Pqrs();
        pqrs3.setTipo(TipoPqrs.SUGERENCIA);
        pqrs3.setDescripcion("Sería útil tener filtros por precio en la búsqueda de productos.");
        pqrs3.setEstado(EstadoPqrs.RESPONDIDA);
        pqrs3.setEmisor(proveedor1);
        pqrs3.setReceptor(proveedor1);
        pqrs3.setPendienteDe(RolProceso.CONSUMIDOR);
        pqrsRepository.save(pqrs3);

        PqrsRespuesta resp3 = new PqrsRespuesta();
        resp3.setPqrs(pqrs3);
        resp3.setContenido("Gracias por tu sugerencia. La consideraremos para futuras actualizaciones.");
        resp3.setEmisor(admin);
        resp3.setEmitidoPor(RolProceso.PROVEEDOR);
        resp3.setFechaEmision(LocalDateTime.now().minusDays(2));
        pqrsRespuestaRepository.save(resp3);
        pqrsCreadas++;

        System.out.println("    ✓ " + pqrsCreadas + " PQRS creadas\n");
    }
}

