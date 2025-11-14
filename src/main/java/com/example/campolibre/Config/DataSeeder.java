// ==================== DataSeeder.java ====================
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

    @Override
    public void run(String... args) throws Exception {
        // Solo crear datos si no existen
        if (tiendaRepository.count() == 0) {
            System.out.println("📦 Iniciando carga de datos de prueba...");

            cargarTiendas();
            cargarProductos();
            cargarEventos();
            cargarMisEventos();
            cargarPqrs();

            System.out.println("✅ Datos de prueba cargados exitosamente!");
        } else {
            System.out.println("ℹ️ Ya existen datos en la base de datos. Omitiendo seeder.");
        }
    }

    private void cargarTiendas() {
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

        // ⭐ Tienda 3 - ACTUALIZADA (ya no vende herramientas)
        Tienda tienda3 = new Tienda();
        tienda3.setNombre("Granos y Cereales Don Juan");
        tienda3.setDescripcion("Granos secos, cereales y semillas de la mejor calidad. Productos seleccionados y empacados con higiene.");
        tienda3.setEstado(EstadoTienda.ACTIVA);
        tienda3.setCorreo_tienda("ventas@granosdj.com");
        tienda3.setTelefono_tienda("3157654321");
        tienda3.setUbicacion("Centro Agroalimentario, Bogotá");
        tienda3.setUsuario(admin);
        tienda3.setImagen_tienda(null);
        tiendaRepository.save(tienda3);
        System.out.println("✓ Tienda creada: " + tienda3.getNombre());

        // ⭐ Tienda 4 - NUEVA (Verduras)
        Tienda tienda4 = new Tienda();
        tienda4.setNombre("Verduras Frescas El Huerto");
        tienda4.setDescripcion("Verduras y hortalizas recién cosechadas. Cultivo hidropónico y orgánico.");
        tienda4.setEstado(EstadoTienda.ACTIVA);
        tienda4.setCorreo_tienda("contacto@elhuerto.com");
        tienda4.setTelefono_tienda("3118887766");
        tienda4.setUbicacion("Km 5 Vía Chía, Cundinamarca");
        tienda4.setUsuario(proveedor);
        tienda4.setImagen_tienda(null);
        tiendaRepository.save(tienda4);
        System.out.println("✓ Tienda creada: " + tienda4.getNombre());
    }

    private void cargarProductos() {
        Tienda tienda1 = tiendaRepository.findById(1L).orElse(null);
        Tienda tienda2 = tiendaRepository.findById(2L).orElse(null);
        Tienda tienda3 = tiendaRepository.findById(3L).orElse(null);
        Tienda tienda4 = tiendaRepository.findById(4L).orElse(null);

        if (tienda1 == null || tienda2 == null || tienda3 == null || tienda4 == null) {
            System.out.println("⚠️ No se encontraron tiendas para agregar productos");
            return;
        }

        // ==================== PRODUCTOS DE TIENDA 1 (FRUTAS) ====================

        // Producto 1: Aguacate Hass
        Producto p1 = new Producto();
        p1.setNombre("Aguacate Hass");
        p1.setDescripcion("Aguacate de primera calidad, cremoso y nutritivo. Perfecto para ensaladas y preparaciones.");
        p1.setPrecio(2500.0);
        p1.setStock(50);
        p1.setCategoria(CategoriaProducto.FRUTAS);
        p1.setSubcategoria(SubcategoriaProducto.TROPICALES);
        p1.setCantidad(1.0);
        p1.setUnidadMedida(UnidadMedida.KILOGRAMO);
        p1.setTienda(tienda1);
        p1.setImagen_producto(null);
        p1.setEstado("ACTIVO");
        productoRepository.save(p1);

        // Producto 2: Mango Tommy
        Producto p2 = new Producto();
        p2.setNombre("Mango Tommy");
        p2.setDescripcion("Mango dulce y jugoso, ideal para jugos y postres. Cosecha reciente.");
        p2.setPrecio(3000.0);
        p2.setStock(30);
        p2.setCategoria(CategoriaProducto.FRUTAS);
        p2.setSubcategoria(SubcategoriaProducto.TROPICALES);
        p2.setCantidad(1.0);
        p2.setUnidadMedida(UnidadMedida.KILOGRAMO);
        p2.setTienda(tienda1);
        p2.setImagen_producto(null);
        p2.setEstado("ACTIVO");
        productoRepository.save(p2);

        // Producto 3: Papaya Maradol
        Producto p3 = new Producto();
        p3.setNombre("Papaya Maradol");
        p3.setDescripcion("Papaya grande y dulce, rica en vitaminas. Excelente para el desayuno.");
        p3.setPrecio(4500.0);
        p3.setStock(20);
        p3.setCategoria(CategoriaProducto.FRUTAS);
        p3.setSubcategoria(SubcategoriaProducto.TROPICALES);
        p3.setCantidad(1.0);
        p3.setUnidadMedida(UnidadMedida.UNIDAD);
        p3.setTienda(tienda1);
        p3.setImagen_producto(null);
        p3.setEstado("ACTIVO");
        productoRepository.save(p3);

        // Producto 4: Naranja Valencia
        Producto p4 = new Producto();
        p4.setNombre("Naranja Valencia");
        p4.setDescripcion("Naranjas jugosas, ideales para jugo. Alto contenido de vitamina C.");
        p4.setPrecio(2000.0);
        p4.setStock(8); // ⚠️ Stock bajo para pruebas
        p4.setCategoria(CategoriaProducto.FRUTAS);
        p4.setSubcategoria(SubcategoriaProducto.CITRICOS);
        p4.setCantidad(1.0);
        p4.setUnidadMedida(UnidadMedida.KILOGRAMO);
        p4.setTienda(tienda1);
        p4.setImagen_producto(null);
        p4.setEstado("ACTIVO");
        productoRepository.save(p4);

        System.out.println("✓ 4 productos de FRUTAS creados");

        // ==================== PRODUCTOS DE TIENDA 2 (LÁCTEOS) ====================

        // Producto 5: Queso Campesino
        Producto p5 = new Producto();
        p5.setNombre("Queso Campesino");
        p5.setDescripcion("Queso fresco artesanal. Elaborado con leche del día, sin conservantes.");
        p5.setPrecio(12000.0);
        p5.setStock(25);
        p5.setCategoria(CategoriaProducto.LACTEOS);
        p5.setSubcategoria(SubcategoriaProducto.QUESO_FRESCO);
        p5.setCantidad(0.5);
        p5.setUnidadMedida(UnidadMedida.KILOGRAMO);
        p5.setTienda(tienda2);
        p5.setImagen_producto(null);
        p5.setEstado("ACTIVO");
        productoRepository.save(p5);

        // Producto 6: Yogurt Natural
        Producto p6 = new Producto();
        p6.setNombre("Yogurt Natural");
        p6.setDescripcion("Yogurt casero, sin azúcar añadida. Probióticos naturales.");
        p6.setPrecio(8000.0);
        p6.setStock(40);
        p6.setCategoria(CategoriaProducto.LACTEOS);
        p6.setSubcategoria(SubcategoriaProducto.YOGURT);
        p6.setCantidad(1.0);
        p6.setUnidadMedida(UnidadMedida.LITRO);
        p6.setTienda(tienda2);
        p6.setImagen_producto(null);
        p6.setEstado("ACTIVO");
        productoRepository.save(p6);

        // Producto 7: Mantequilla Artesanal
        Producto p7 = new Producto();
        p7.setNombre("Mantequilla Artesanal");
        p7.setDescripcion("Mantequilla casera. Sabor único y tradicional.");
        p7.setPrecio(6500.0);
        p7.setStock(15);
        p7.setCategoria(CategoriaProducto.LACTEOS);
        p7.setSubcategoria(SubcategoriaProducto.MANTEQUILLA);
        p7.setCantidad(250.0);
        p7.setUnidadMedida(UnidadMedida.GRAMO);
        p7.setTienda(tienda2);
        p7.setImagen_producto(null);
        p7.setEstado("ACTIVO");
        productoRepository.save(p7);

        // Producto 8: Leche Entera
        Producto p8 = new Producto();
        p8.setNombre("Leche Entera Fresca");
        p8.setDescripcion("Leche fresca de vaca, pasteurizada. Ideal para toda la familia.");
        p8.setPrecio(3500.0);
        p8.setStock(4); // ⚠️ Stock crítico para pruebas
        p8.setCategoria(CategoriaProducto.LACTEOS);
        p8.setSubcategoria(SubcategoriaProducto.LECHE_ENTERA);
        p8.setCantidad(1.0);
        p8.setUnidadMedida(UnidadMedida.LITRO);
        p8.setTienda(tienda2);
        p8.setImagen_producto(null);
        p8.setEstado("ACTIVO");
        productoRepository.save(p8);

        System.out.println("✓ 4 productos de LÁCTEOS creados");

        // ==================== PRODUCTOS DE TIENDA 3 (GRANOS Y CEREALES) ====================

        // Producto 9: Frijol Cargamanto
        Producto p9 = new Producto();
        p9.setNombre("Frijol Cargamanto");
        p9.setDescripcion("Frijol rojo de excelente calidad. Limpio y seleccionado.");
        p9.setPrecio(7000.0);
        p9.setStock(100);
        p9.setCategoria(CategoriaProducto.GRANOS);
        p9.setSubcategoria(SubcategoriaProducto.FRIJOLES);
        p9.setCantidad(1.0);
        p9.setUnidadMedida(UnidadMedida.KILOGRAMO);
        p9.setTienda(tienda3);
        p9.setImagen_producto(null);
        p9.setEstado("ACTIVO");
        productoRepository.save(p9);

        // Producto 10: Arroz Blanco
        Producto p10 = new Producto();
        p10.setNombre("Arroz Blanco Especial");
        p10.setDescripcion("Arroz de grano largo, excelente para todo tipo de preparaciones.");
        p10.setPrecio(4500.0);
        p10.setStock(80);
        p10.setCategoria(CategoriaProducto.CEREALES);
        p10.setSubcategoria(SubcategoriaProducto.ARROZ);
        p10.setCantidad(1.0);
        p10.setUnidadMedida(UnidadMedida.KILOGRAMO);
        p10.setTienda(tienda3);
        p10.setImagen_producto(null);
        p10.setEstado("ACTIVO");
        productoRepository.save(p10);

        // Producto 11: Avena en Hojuelas
        Producto p11 = new Producto();
        p11.setNombre("Avena en Hojuelas");
        p11.setDescripcion("Avena 100% natural, perfecta para el desayuno.");
        p11.setPrecio(5500.0);
        p11.setStock(60);
        p11.setCategoria(CategoriaProducto.CEREALES);
        p11.setSubcategoria(SubcategoriaProducto.AVENA);
        p11.setCantidad(500.0);
        p11.setUnidadMedida(UnidadMedida.GRAMO);
        p11.setTienda(tienda3);
        p11.setImagen_producto(null);
        p11.setEstado("ACTIVO");
        productoRepository.save(p11);

        // Producto 12: Lentejas
        Producto p12 = new Producto();
        p12.setNombre("Lentejas Rojas");
        p12.setDescripcion("Lentejas de cocción rápida, ricas en proteína vegetal.");
        p12.setPrecio(6000.0);
        p12.setStock(9); // ⚠️ Stock bajo para pruebas
        p12.setCategoria(CategoriaProducto.GRANOS);
        p12.setSubcategoria(SubcategoriaProducto.LENTEJAS);
        p12.setCantidad(500.0);
        p12.setUnidadMedida(UnidadMedida.GRAMO);
        p12.setTienda(tienda3);
        p12.setImagen_producto(null);
        p12.setEstado("ACTIVO");
        productoRepository.save(p12);

        System.out.println("✓ 4 productos de GRANOS Y CEREALES creados");

        // ==================== PRODUCTOS DE TIENDA 4 (VERDURAS) ====================

        // Producto 13: Tomate Chonto
        Producto p13 = new Producto();
        p13.setNombre("Tomate Chonto Orgánico");
        p13.setDescripcion("Tomate fresco y jugoso, cultivado sin pesticidas.");
        p13.setPrecio(3500.0);
        p13.setStock(45);
        p13.setCategoria(CategoriaProducto.VERDURAS);
        p13.setSubcategoria(SubcategoriaProducto.HORTALIZAS_FRUTO);
        p13.setCantidad(1.0);
        p13.setUnidadMedida(UnidadMedida.KILOGRAMO);
        p13.setTienda(tienda4);
        p13.setImagen_producto(null);
        p13.setEstado("ACTIVO");
        productoRepository.save(p13);

        // Producto 14: Lechuga Crespa
        Producto p14 = new Producto();
        p14.setNombre("Lechuga Crespa");
        p14.setDescripcion("Lechuga fresca y crujiente, ideal para ensaladas.");
        p14.setPrecio(2500.0);
        p14.setStock(35);
        p14.setCategoria(CategoriaProducto.VERDURAS);
        p14.setSubcategoria(SubcategoriaProducto.HORTALIZAS_HOJA);
        p14.setCantidad(1.0);
        p14.setUnidadMedida(UnidadMedida.UNIDAD);
        p14.setTienda(tienda4);
        p14.setImagen_producto(null);
        p14.setEstado("ACTIVO");
        productoRepository.save(p14);

        // Producto 15: Papa Criolla
        Producto p15 = new Producto();
        p15.setNombre("Papa Criolla");
        p15.setDescripcion("Papa criolla de la mejor calidad, recién cosechada.");
        p15.setPrecio(4000.0);
        p15.setStock(3); // ⚠️ Stock crítico para pruebas
        p15.setCategoria(CategoriaProducto.VERDURAS);
        p15.setSubcategoria(SubcategoriaProducto.TUBERCULOS);
        p15.setCantidad(1.0);
        p15.setUnidadMedida(UnidadMedida.KILOGRAMO);
        p15.setTienda(tienda4);
        p15.setImagen_producto(null);
        p15.setEstado("ACTIVO");
        productoRepository.save(p15);

        // Producto 16: Zanahoria
        Producto p16 = new Producto();
        p16.setNombre("Zanahoria");
        p16.setDescripcion("Zanahoria fresca, dulce y crujiente. Rica en betacaroteno.");
        p16.setPrecio(2800.0);
        p16.setStock(0); // ⚠️ Agotado para pruebas
        p16.setCategoria(CategoriaProducto.VERDURAS);
        p16.setSubcategoria(SubcategoriaProducto.RAICES);
        p16.setCantidad(1.0);
        p16.setUnidadMedida(UnidadMedida.KILOGRAMO);
        p16.setTienda(tienda4);
        p16.setImagen_producto(null);
        p16.setEstado("ACTIVO");
        productoRepository.save(p16);

        System.out.println("✓ 4 productos de VERDURAS creados");
        System.out.println("✅ Total: 16 productos creados exitosamente");
        System.out.println("   📊 Productos con stock bajo: 4 (para pruebas de alertas)");
        System.out.println("   📊 Productos agotados: 1 (para pruebas)");
    }

    private void cargarEventos() {
        Usuario admin = usuarioRepository.findByEmail("admin@campolibre.com");
        Usuario proveedor = usuarioRepository.findByEmail("proveedor@campolibre.com");

        // Evento 1 - APROBADO
        Evento evento1 = new Evento();
        evento1.setNombre("Feria Agrícola Regional 2025");
        evento1.setDescripcion("Gran feria donde productores del campo mostrarán sus mejores productos. " +
                "Habrá degustaciones, música en vivo y actividades para toda la familia.");
        evento1.setUbicacion("Parque Principal de Fusagasugá");
        evento1.setFecha_evento(LocalDate.now().plusDays(15));
        evento1.setHora_evento(LocalTime.of(9, 0));
        evento1.setTipo_evento(TipoEvento.FERIA);
        evento1.setEstado(EstadoEvento.APROBADO);
        evento1.setCreado_por(proveedor);
        evento1.setImagen_evento(null);
        eventoRepository.save(evento1);
        System.out.println("✓ Evento creado: " + evento1.getNombre());

        // Evento 2 - APROBADO
        Evento evento2 = new Evento();
        evento2.setNombre("Taller de Agricultura Sostenible");
        evento2.setDescripcion("Aprende técnicas modernas de cultivo orgánico y sostenible. " +
                "Incluye certificado de participación y kit de semillas.");
        evento2.setUbicacion("Auditorio SENA, Centro Agroindustrial");
        evento2.setFecha_evento(LocalDate.now().plusDays(7));
        evento2.setHora_evento(LocalTime.of(14, 0));
        evento2.setTipo_evento(TipoEvento.TALLER);
        evento2.setEstado(EstadoEvento.APROBADO);
        evento2.setCreado_por(admin);
        evento2.setImagen_evento(null);
        eventoRepository.save(evento2);
        System.out.println("✓ Evento creado: " + evento2.getNombre());

        // Evento 3 - PENDIENTE (para que admin lo apruebe)
        Evento evento3 = new Evento();
        evento3.setNombre("Día del Campesino - Celebración");
        evento3.setDescripcion("Celebración especial en honor a los campesinos de la región. " +
                "Comida típica, presentaciones culturales y reconocimientos.");
        evento3.setUbicacion("Plaza de Mercado Municipal");
        evento3.setFecha_evento(LocalDate.now().plusDays(30));
        evento3.setHora_evento(LocalTime.of(10, 0));
        evento3.setTipo_evento(TipoEvento.ACTIVIDAD);
        evento3.setEstado(EstadoEvento.PENDIENTE);
        evento3.setCreado_por(proveedor);
        evento3.setImagen_evento(null);
        eventoRepository.save(evento3);
        System.out.println("✓ Evento creado: " + evento3.getNombre() + " (PENDIENTE)");
    }

    private void cargarMisEventos() {
        Usuario consumidor = usuarioRepository.findByEmail("consumidor@campolibre.com");
        Usuario admin = usuarioRepository.findByEmail("admin@campolibre.com");

        Evento evento1 = eventoRepository.findById(1L).orElse(null);
        Evento evento2 = eventoRepository.findById(2L).orElse(null);

        if (evento1 == null || evento2 == null || consumidor == null) {
            System.out.println("⚠️ No se encontraron eventos o usuarios para confirmaciones");
            return;
        }

        // Confirmación 1 - Consumidor va a la Feria
        MisEventos me1 = new MisEventos();
        me1.setUsuario(consumidor);
        me1.setEvento(evento1);
        misEventosRepository.save(me1);

        // Confirmación 2 - Admin va al Taller
        MisEventos me2 = new MisEventos();
        me2.setUsuario(admin);
        me2.setEvento(evento2);
        misEventosRepository.save(me2);

        System.out.println("✓ 2 confirmaciones de asistencia creadas");
    }

    private void cargarPqrs() {
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