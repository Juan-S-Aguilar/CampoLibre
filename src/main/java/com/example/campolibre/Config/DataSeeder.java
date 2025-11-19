package com.example.campolibre.Config;

import com.example.campolibre.Entity.*;
import com.example.campolibre.Enum.*;
import com.example.campolibre.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2) // Se ejecuta después de DataLoader
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TiendaRepository tiendaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public void run(String... args) throws Exception {
        // Solo crear datos si no existen tiendas
        if (tiendaRepository.count() == 0) {
            System.out.println("📦 Iniciando carga de datos de prueba...");

            cargarTiendas();
            cargarProductos();

            System.out.println("✅ Datos de prueba cargados exitosamente!");
        } else {
            System.out.println("ℹ️ Ya existen datos en la base de datos. No se cargan datos de prueba.");
        }
    }

    private void cargarTiendas() {
        System.out.println("🏪 Cargando tiendas de prueba...");

        // Obtener usuarios
        Usuario proveedor = usuarioRepository.findByEmail("proveedor@campolibre.com");
        Usuario admin = usuarioRepository.findByEmail("admin@campolibre.com");

        // ========== TIENDA 1: FRUTAS Y VERDURAS ==========
        Tienda tienda1 = new Tienda();
        tienda1.setNombre("Finca El Paraíso");
        tienda1.setDescripcion("Frutas y verduras frescas directamente del campo. Productos orgánicos de la mejor calidad.");
        tienda1.setCategoriaPrincipal(CategoriaTienda.FRUTAS_VERDURAS);
        tienda1.setEstado(EstadoTienda.ACTIVA);
        tienda1.setCorreo_tienda("paraiso@campolibre.com");
        tienda1.setTelefono_tienda("3001234567");
        tienda1.setUbicacion("Vereda El Silencio, Medellín");
        tienda1.setUsuario(proveedor);
        tienda1.setImagen_tienda("uploads/tiendas/finca-paraiso.jpg");
        tiendaRepository.save(tienda1);
        System.out.println("✓ Tienda creada: " + tienda1.getNombre());

        // ========== TIENDA 2: PROCESADOS ARTESANALES ==========
        Tienda tienda2 = new Tienda();
        tienda2.setNombre("Sabores del Campo");
        tienda2.setDescripcion("Productos artesanales elaborados con ingredientes 100% naturales. Mermeladas, quesos y panes caseros.");
        tienda2.setCategoriaPrincipal(CategoriaTienda.PROCESADOS_ARTESANALES);
        tienda2.setEstado(EstadoTienda.ACTIVA);
        tienda2.setCorreo_tienda("sabores@campolibre.com");
        tienda2.setTelefono_tienda("3109876543");
        tienda2.setUbicacion("Corregimiento San Cristóbal, Medellín");
        tienda2.setUsuario(admin);
        tienda2.setImagen_tienda("uploads/tiendas/sabores-campo.jpg");
        tiendaRepository.save(tienda2);
        System.out.println("✓ Tienda creada: " + tienda2.getNombre());

        // ========== TIENDA 3: GRANOS Y SEMILLAS ==========
        Tienda tienda3 = new Tienda();
        tienda3.setNombre("Cosecha Dorada");
        tienda3.setDescripcion("Granos, cereales y semillas de la mejor calidad. Productos cultivados con técnicas tradicionales.");
        tienda3.setCategoriaPrincipal(CategoriaTienda.GRANOS_SEMILLAS);
        tienda3.setEstado(EstadoTienda.ACTIVA);
        tienda3.setCorreo_tienda("cosecha@campolibre.com");
        tienda3.setTelefono_tienda("3157654321");
        tienda3.setUbicacion("Vereda La Montaña, Rionegro");
        tienda3.setUsuario(proveedor);
        tienda3.setImagen_tienda("uploads/tiendas/cosecha-dorada.jpg");
        tiendaRepository.save(tienda3);
        System.out.println("✓ Tienda creada: " + tienda3.getNombre());

        // ========== TIENDA 4: HIERBAS Y ESPECIAS ==========
        Tienda tienda4 = new Tienda();
        tienda4.setNombre("Aromáticas del Valle");
        tienda4.setDescripcion("Hierbas aromáticas y especias frescas. Cultivamos y secamos nuestras propias plantas medicinales.");
        tienda4.setCategoriaPrincipal(CategoriaTienda.HIERBAS_ESPECIAS);
        tienda4.setEstado(EstadoTienda.ACTIVA);
        tienda4.setCorreo_tienda("aromaticas@campolibre.com");
        tienda4.setTelefono_tienda("3203456789");
        tienda4.setUbicacion("Vereda El Uvito, La Ceja");
        tienda4.setUsuario(admin);
        tienda4.setImagen_tienda("uploads/tiendas/aromaticas-valle.jpg");
        tiendaRepository.save(tienda4);
        System.out.println("✓ Tienda creada: " + tienda4.getNombre());
    }

    private void cargarProductos() {
        System.out.println("🛒 Cargando productos de prueba...");

        // Obtener tiendas
        Tienda tienda1 = tiendaRepository.findByNombre("Finca El Paraíso").get(0);
        Tienda tienda2 = tiendaRepository.findByNombre("Sabores del Campo").get(0);
        Tienda tienda3 = tiendaRepository.findByNombre("Cosecha Dorada").get(0);
        Tienda tienda4 = tiendaRepository.findByNombre("Aromáticas del Valle").get(0);

        // ========== PRODUCTOS DE TIENDA 1: FRUTAS Y VERDURAS ==========

        // Producto 1: Mango
        Producto producto1 = new Producto();
        producto1.setNombre("Mango Tommy");
        producto1.setDescripcion("Mangos frescos y jugosos de excelente sabor. Perfectos para jugos y postres.");
        producto1.setPrecio(3500.0);
        producto1.setStock(50);
        producto1.setStockMinimo(10);
        producto1.setSubcategoria(SubcategoriaProducto.FRUTAS_TROPICALES);
        producto1.setUnidadMedida(UnidadMedida.KILO);
        producto1.setTienda(tienda1);
        producto1.setImagen_producto("uploads/productos/mango.jpg");
        producto1.setEstado("ACTIVO");
        productoRepository.save(producto1);

        // Producto 2: Aguacate
        Producto producto2 = new Producto();
        producto2.setNombre("Aguacate Hass");
        producto2.setDescripcion("Aguacates de excelente calidad, cremosos y con buen sabor.");
        producto2.setPrecio(2800.0);
        producto2.setStock(30);
        producto2.setStockMinimo(8);
        producto2.setSubcategoria(SubcategoriaProducto.FRUTAS_TROPICALES);
        producto2.setUnidadMedida(UnidadMedida.UNIDAD);
        producto2.setTienda(tienda1);
        producto2.setImagen_producto("uploads/productos/aguacate.jpg");
        producto2.setEstado("ACTIVO");
        productoRepository.save(producto2);

        // Producto 3: Naranjas
        Producto producto3 = new Producto();
        producto3.setNombre("Naranjas Valencia");
        producto3.setDescripcion("Naranjas dulces perfectas para jugo natural. Frescas y jugosas.");
        producto3.setPrecio(2500.0);
        producto3.setStock(60);
        producto3.setStockMinimo(15);
        producto3.setSubcategoria(SubcategoriaProducto.FRUTAS_CITRICAS);
        producto3.setUnidadMedida(UnidadMedida.KILO);
        producto3.setTienda(tienda1);
        producto3.setImagen_producto("uploads/productos/naranjas.jpg");
        producto3.setEstado("ACTIVO");
        productoRepository.save(producto3);

        // Producto 4: Lechuga
        Producto producto4 = new Producto();
        producto4.setNombre("Lechuga Crespa");
        producto4.setDescripcion("Lechuga fresca y crujiente, ideal para ensaladas.");
        producto4.setPrecio(1500.0);
        producto4.setStock(25);
        producto4.setStockMinimo(5);
        producto4.setSubcategoria(SubcategoriaProducto.VERDURAS_HOJA);
        producto4.setUnidadMedida(UnidadMedida.UNIDAD);
        producto4.setTienda(tienda1);
        producto4.setImagen_producto("uploads/productos/lechuga.jpg");
        producto4.setEstado("ACTIVO");
        productoRepository.save(producto4);

        // Producto 5: Papa
        Producto producto5 = new Producto();
        producto5.setNombre("Papa Criolla");
        producto5.setDescripcion("Papa criolla de la mejor calidad, perfecta para cualquier preparación.");
        producto5.setPrecio(3200.0);
        producto5.setStock(100);
        producto5.setStockMinimo(20);
        producto5.setSubcategoria(SubcategoriaProducto.TUBERCULOS);
        producto5.setUnidadMedida(UnidadMedida.KILO);
        producto5.setTienda(tienda1);
        producto5.setImagen_producto("uploads/productos/papa-criolla.jpg");
        producto5.setEstado("ACTIVO");
        productoRepository.save(producto5);

        // Producto 6: Tomate
        Producto producto6 = new Producto();
        producto6.setNombre("Tomate Chonto");
        producto6.setDescripcion("Tomates frescos y maduros, ideales para salsas y ensaladas.");
        producto6.setPrecio(2200.0);
        producto6.setStock(40);
        producto6.setStockMinimo(10);
        producto6.setSubcategoria(SubcategoriaProducto.HORTALIZAS);
        producto6.setUnidadMedida(UnidadMedida.KILO);
        producto6.setTienda(tienda1);
        producto6.setImagen_producto("uploads/productos/tomate.jpg");
        producto6.setEstado("ACTIVO");
        productoRepository.save(producto6);

        // ========== PRODUCTOS DE TIENDA 2: PROCESADOS ARTESANALES ==========

        // Producto 7: Mermelada de Mora
        Producto producto7 = new Producto();
        producto7.setNombre("Mermelada de Mora");
        producto7.setDescripcion("Mermelada artesanal elaborada con moras frescas del campo.");
        producto7.setPrecio(8500.0);
        producto7.setStock(20);
        producto7.setStockMinimo(5);
        producto7.setSubcategoria(SubcategoriaProducto.MERMELADAS);
        producto7.setUnidadMedida(UnidadMedida.UNIDAD);
        producto7.setTienda(tienda2);
        producto7.setImagen_producto("uploads/productos/mermelada-mora.jpg");
        producto7.setEstado("ACTIVO");
        productoRepository.save(producto7);

        // Producto 8: Queso Campesino
        Producto producto8 = new Producto();
        producto8.setNombre("Queso Campesino");
        producto8.setDescripcion("Queso fresco artesanal elaborado con leche de vaca. Sabor tradicional.");
        producto8.setPrecio(12000.0);
        producto8.setStock(15);
        producto8.setStockMinimo(3);
        producto8.setSubcategoria(SubcategoriaProducto.QUESOS);
        producto8.setUnidadMedida(UnidadMedida.LIBRA);
        producto8.setTienda(tienda2);
        producto8.setImagen_producto("uploads/productos/queso-campesino.jpg");
        producto8.setEstado("ACTIVO");
        productoRepository.save(producto8);

        // Producto 9: Pan Integral
        Producto producto9 = new Producto();
        producto9.setNombre("Pan Integral");
        producto9.setDescripcion("Pan artesanal elaborado con harinas integrales y semillas.");
        producto9.setPrecio(6000.0);
        producto9.setStock(12);
        producto9.setStockMinimo(3);
        producto9.setSubcategoria(SubcategoriaProducto.PAN_ARTESANAL);
        producto9.setUnidadMedida(UnidadMedida.UNIDAD);
        producto9.setTienda(tienda2);
        producto9.setImagen_producto("uploads/productos/pan-integral.jpg");
        producto9.setEstado("ACTIVO");
        productoRepository.save(producto9);

        // Producto 10: Salsa de Tomate
        Producto producto10 = new Producto();
        producto10.setNombre("Salsa de Tomate Casera");
        producto10.setDescripcion("Salsa de tomate artesanal sin conservantes ni colorantes.");
        producto10.setPrecio(7500.0);
        producto10.setStock(18);
        producto10.setStockMinimo(5);
        producto10.setSubcategoria(SubcategoriaProducto.SALSAS);
        producto10.setUnidadMedida(UnidadMedida.UNIDAD);
        producto10.setTienda(tienda2);
        producto10.setImagen_producto("uploads/productos/salsa-tomate.jpg");
        producto10.setEstado("ACTIVO");
        productoRepository.save(producto10);

        // ========== PRODUCTOS DE TIENDA 3: GRANOS Y SEMILLAS ==========

        // Producto 11: Frijol Rojo
        Producto producto11 = new Producto();
        producto11.setNombre("Frijol Rojo");
        producto11.setDescripcion("Frijol rojo de primera calidad, cultivado tradicionalmente.");
        producto11.setPrecio(5500.0);
        producto11.setStock(80);
        producto11.setStockMinimo(15);
        producto11.setSubcategoria(SubcategoriaProducto.LEGUMINOSAS);
        producto11.setUnidadMedida(UnidadMedida.KILO);
        producto11.setTienda(tienda3);
        producto11.setImagen_producto("uploads/productos/frijol-rojo.jpg");
        producto11.setEstado("ACTIVO");
        productoRepository.save(producto11);

        // Producto 12: Maíz Amarillo
        Producto producto12 = new Producto();
        producto12.setNombre("Maíz Amarillo");
        producto12.setDescripcion("Maíz amarillo de excelente calidad para arepas y mazorcas.");
        producto12.setPrecio(4200.0);
        producto12.setStock(100);
        producto12.setStockMinimo(20);
        producto12.setSubcategoria(SubcategoriaProducto.CEREALES);
        producto12.setUnidadMedida(UnidadMedida.KILO);
        producto12.setTienda(tienda3);
        producto12.setImagen_producto("uploads/productos/maiz-amarillo.jpg");
        producto12.setEstado("ACTIVO");
        productoRepository.save(producto12);

        // Producto 13: Semillas de Chía
        Producto producto13 = new Producto();
        producto13.setNombre("Semillas de Chía");
        producto13.setDescripcion("Semillas de chía naturales, ricas en omega 3 y fibra.");
        producto13.setPrecio(8000.0);
        producto13.setStock(25);
        producto13.setStockMinimo(5);
        producto13.setSubcategoria(SubcategoriaProducto.SEMILLAS);
        producto13.setUnidadMedida(UnidadMedida.BOLSA_1KG);
        producto13.setTienda(tienda3);
        producto13.setImagen_producto("uploads/productos/chia.jpg");
        producto13.setEstado("ACTIVO");
        productoRepository.save(producto13);

        // Producto 14: Harina de Trigo Integral
        Producto producto14 = new Producto();
        producto14.setNombre("Harina de Trigo Integral");
        producto14.setDescripcion("Harina integral molida artesanalmente, perfecta para panes y repostería.");
        producto14.setPrecio(6500.0);
        producto14.setStock(30);
        producto14.setStockMinimo(8);
        producto14.setSubcategoria(SubcategoriaProducto.HARINAS_ARTESANALES);
        producto14.setUnidadMedida(UnidadMedida.KILO);
        producto14.setTienda(tienda3);
        producto14.setImagen_producto("uploads/productos/harina-integral.jpg");
        producto14.setEstado("ACTIVO");
        productoRepository.save(producto14);

        // ========== PRODUCTOS DE TIENDA 4: HIERBAS Y ESPECIAS ==========

        // Producto 15: Albahaca Fresca
        Producto producto15 = new Producto();
        producto15.setNombre("Albahaca Fresca");
        producto15.setDescripcion("Albahaca fresca cultivada orgánicamente, ideal para pastas y ensaladas.");
        producto15.setPrecio(3000.0);
        producto15.setStock(35);
        producto15.setStockMinimo(8);
        producto15.setSubcategoria(SubcategoriaProducto.HIERBAS_AROMATICAS);
        producto15.setUnidadMedida(UnidadMedida.PAQUETE);
        producto15.setTienda(tienda4);
        producto15.setImagen_producto("uploads/productos/albahaca.jpg");
        producto15.setEstado("ACTIVO");
        productoRepository.save(producto15);

        // Producto 16: Canela en Rama
        Producto producto16 = new Producto();
        producto16.setNombre("Canela en Rama");
        producto16.setDescripcion("Canela en rama de excelente aroma y sabor.");
        producto16.setPrecio(4500.0);
        producto16.setStock(20);
        producto16.setStockMinimo(5);
        producto16.setSubcategoria(SubcategoriaProducto.ESPECIAS_SECAS);
        producto16.setUnidadMedida(UnidadMedida.PAQUETE);
        producto16.setTienda(tienda4);
        producto16.setImagen_producto("uploads/productos/canela.jpg");
        producto16.setEstado("ACTIVO");
        productoRepository.save(producto16);

        // Producto 17: Mezcla de Especias para Carne
        Producto producto17 = new Producto();
        producto17.setNombre("Mezcla Especias Carne");
        producto17.setDescripcion("Mezcla artesanal de especias para sazonar carnes a la parrilla.");
        producto17.setPrecio(5500.0);
        producto17.setStock(15);
        producto17.setStockMinimo(3);
        producto17.setSubcategoria(SubcategoriaProducto.MEZCLAS_CONDIMENTOS);
        producto17.setUnidadMedida(UnidadMedida.PAQUETE);
        producto17.setTienda(tienda4);
        producto17.setImagen_producto("uploads/productos/mezcla-especias.jpg");
        producto17.setEstado("ACTIVO");
        productoRepository.save(producto17);

        // Producto 18: Té de Manzanilla
        Producto producto18 = new Producto();
        producto18.setNombre("Té de Manzanilla");
        producto18.setDescripcion("Manzanilla natural para infusiones relajantes.");
        producto18.setPrecio(4000.0);
        producto18.setStock(40);
        producto18.setStockMinimo(10);
        producto18.setSubcategoria(SubcategoriaProducto.INFUSIONES_TES);
        producto18.setUnidadMedida(UnidadMedida.PAQUETE);
        producto18.setTienda(tienda4);
        producto18.setImagen_producto("uploads/productos/manzanilla.jpg");
        producto18.setEstado("ACTIVO");
        productoRepository.save(producto18);

        // Producto 19: Producto con stock bajo (para probar alertas)
        Producto producto19 = new Producto();
        producto19.setNombre("Orégano Seco");
        producto19.setDescripcion("Orégano seco de aroma intenso, perfecto para pizzas y pastas.");
        producto19.setPrecio(3500.0);
        producto19.setStock(3); // Stock bajo
        producto19.setStockMinimo(5);
        producto19.setSubcategoria(SubcategoriaProducto.HIERBAS_AROMATICAS);
        producto19.setUnidadMedida(UnidadMedida.PAQUETE);
        producto19.setTienda(tienda4);
        producto19.setImagen_producto("uploads/productos/oregano.jpg");
        producto19.setEstado("ACTIVO");
        productoRepository.save(producto19);

        // Producto 20: Producto sin stock (para probar sistema de inventario)
        Producto producto20 = new Producto();
        producto20.setNombre("Tomillo Fresco");
        producto20.setDescripcion("Tomillo fresco para condimentar carnes y guisos.");
        producto20.setPrecio(3200.0);
        producto20.setStock(0); // Sin stock
        producto20.setStockMinimo(5);
        producto20.setSubcategoria(SubcategoriaProducto.HIERBAS_AROMATICAS);
        producto20.setUnidadMedida(UnidadMedida.PAQUETE);
        producto20.setTienda(tienda4);
        producto20.setImagen_producto("uploads/productos/tomillo.jpg");
        producto20.setEstado("SIN_STOCK"); // Se desactiva automáticamente
        productoRepository.save(producto20);

        System.out.println("✓ " + productoRepository.count() + " productos creados exitosamente");
    }
}