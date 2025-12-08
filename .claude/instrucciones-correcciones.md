# Instrucciones de Corrección: Módulo Tiendas y Productos
## Proyecto Campo Libre - Defectos encontrados en Pruebas

---

## **CONTEXTO DEL PROYECTO**

**Nombre:** Campo Libre  
**Tipo:** Aplicación web tipo Marketplace agrícola  
**Stack:** Spring Boot + Thymeleaf + MySQL  
**Arquitectura:** MVC

### **Roles del Sistema:**
- **Administrador:** Acceso total, gestión de PQRS y eventos
- **Consumidor:** Comprar productos, ver eventos, crear PQRS
- **Proveedor:** Crear tiendas, gestionar inventario, responder PQRS

### **Estados Actuales:**
- **Tienda:** `ACTIVA`, `INACTIVA`, `ELIMINADA` (EstadoTienda enum)
- **Producto:** `ACTIVO`, `INACTIVO`, `ELIMINADO`, `SIN_STOCK` (String)

---

## **DEFECTOS IDENTIFICADOS Y SOLUCIONES**

### **1. Quitar Botón "Todas" en vista tienda/list para Proveedor**

**Problema:**  
En la vista `tienda/list.html`, el proveedor tiene un botón "Todas" que no tiene sentido, ya que solo debe ver sus propias tiendas.

**Solución:**
- **Archivo:** `src/main/resources/templates/tienda/list.html`
- **Acción:** Modificar la sección de filtros/botones para que el botón "Todas" solo sea visible para `ADMINISTRADOR`.
- **Condición a agregar:** `sec:authorize="hasAuthority('ADMINISTRADOR')"` o `th:if="${tipoLista == 'todas'}"`

**Ubicación aproximada en el código:**  
Buscar el botón con texto "Todas" o similar, y envolver en condición Thymeleaf que verifique el rol.

**Ejemplo esperado:**
```html
<a th:href="@{/tiendas?categoria=}" 
   class="filter-button"
   th:if="${tipoLista == 'todas'}"
   sec:authorize="hasAuthority('ADMINISTRADOR')">
    <i class="bi bi-shop"></i> Todas
</a>
```

---

### **2. Desactivar/Inactivar Tienda para Admin + Cascada a Productos**

**Problema:**  
El administrador debe poder cambiar el estado de una tienda entre `ACTIVA` e `INACTIVA`. Cuando una tienda se inactiva:
1. No debe mostrarse en `tienda/list` para Consumidor
2. Los productos de esa tienda deben cambiar automáticamente a `INACTIVO`
3. Los productos inactivos no deben poder:
    - Aumentar/disminuir stock
    - Ser comprados
    - Visualizarse en `producto/list`

**Solución:**

#### **Backend - Servicio:**
**Archivo:** `src/main/java/com/example/campolibre/Service/TiendaService.java`

Agregar métodos:
```java
TiendaDTO activarTienda(Long idTienda);
TiendaDTO inactivarTienda(Long idTienda);
```

**Archivo:** `src/main/java/com/example/campolibre/Implement/TiendaImplement.java`

Implementar métodos con lógica de cascada:
```java
@Override
@Transactional
public TiendaDTO activarTienda(Long idTienda) {
    Tienda tienda = tiendaRepository.findById(idTienda)
        .orElseThrow(() -> new CustomException("Tienda no encontrada"));
    
    // Cambiar estado tienda a ACTIVA
    tienda.setEstado(EstadoTienda.ACTIVA);
    
    // CASCADA: Activar productos que NO estén ELIMINADOS
    List<Producto> productos = productoRepository.findAllByTiendaId(idTienda);
    for (Producto p : productos) {
        if (!"ELIMINADO".equals(p.getEstado())) {
            // Si tiene stock, ACTIVO; si no, SIN_STOCK
            p.setEstado(p.getStock() > 0 ? "ACTIVO" : "SIN_STOCK");
        }
    }
    productoRepository.saveAll(productos);
    
    tiendaRepository.save(tienda);
    return convertirADTO(tienda);
}

@Override
@Transactional
public TiendaDTO inactivarTienda(Long idTienda) {
    Tienda tienda = tiendaRepository.findById(idTienda)
        .orElseThrow(() -> new CustomException("Tienda no encontrada"));
    
    // Cambiar estado tienda a INACTIVA
    tienda.setEstado(EstadoTienda.INACTIVA);
    
    // CASCADA: Inactivar todos los productos (excepto ELIMINADOS)
    List<Producto> productos = productoRepository.findAllByTiendaId(idTienda);
    for (Producto p : productos) {
        if (!"ELIMINADO".equals(p.getEstado())) {
            p.setEstado("INACTIVO");
        }
    }
    productoRepository.saveAll(productos);
    
    tiendaRepository.save(tienda);
    return convertirADTO(tienda);
}
```

#### **Backend - Controller:**
**Archivo:** `src/main/java/com/example/campolibre/Controller/TiendaController.java`

Agregar endpoints:
```java
@PostMapping("/activar/{id}")
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public String activarTienda(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
        tiendaService.activarTienda(id);
        redirectAttributes.addFlashAttribute("success", "Tienda activada correctamente");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/tiendas";
}

@PostMapping("/inactivar/{id}")
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public String inactivarTienda(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
        tiendaService.inactivarTienda(id);
        redirectAttributes.addFlashAttribute("success", "Tienda inactivada correctamente");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/tiendas";
}
```

#### **Frontend - Vista Administrador:**
**Archivo:** `src/main/resources/templates/tienda/list.html`

Modificar columna de acciones para ADMINISTRADOR:
```html
<!-- Dentro de la columna Acciones, agregar botones condicionales -->
<div sec:authorize="hasAuthority('ADMINISTRADOR')">
    <form th:action="@{/tiendas/inactivar/{id}(id=${tienda.id_tienda})}" 
          method="post" 
          style="display: inline;"
          th:if="${tienda.estado.name() == 'ACTIVA'}">
        <button type="submit" 
                class="btn-action btn-warning" 
                title="Inactivar tienda"
                onclick="return confirm('¿Está seguro de inactivar esta tienda? Los productos también se inactivarán.')">
            <i class="bi bi-pause-circle"></i>
        </button>
    </form>
    
    <form th:action="@{/tiendas/activar/{id}(id=${tienda.id_tienda})}" 
          method="post" 
          style="display: inline;"
          th:if="${tienda.estado.name() == 'INACTIVA'}">
        <button type="submit" 
                class="btn-action btn-success" 
                title="Activar tienda">
            <i class="bi bi-check-circle"></i>
        </button>
    </form>
</div>
```

#### **Filtrado en Vistas:**
**Archivo:** `src/main/java/com/example/campolibre/Implement/TiendaImplement.java`

Verificar que el método `obtenerTiendasActivas()` excluya INACTIVAS:
```java
@Override
public List<TiendaDTO> obtenerTiendasActivas() {
    List<Tienda> tiendas = tiendaRepository.findByEstado(EstadoTienda.ACTIVA);
    return tiendas.stream()
        .map(this::convertirADTO)
        .collect(Collectors.toList());
}
```

**Archivo:** `src/main/java/com/example/campolibre/Repository/TiendaRepository.java`

Agregar query si no existe:
```java
@Query("SELECT t FROM Tienda t WHERE t.estado = :estado")
List<Tienda> findByEstado(@Param("estado") EstadoTienda estado);
```

#### **Bloqueo de Acciones en Productos Inactivos:**
**Archivo:** `src/main/resources/templates/inventario/panel.html`

Deshabilitar botones de stock si producto está INACTIVO:
```html
<button type="button" 
        onclick="actualizarStock(...)"
        th:disabled="${producto.estado == 'INACTIVO' or producto.estado == 'ELIMINADO'}"
        class="btn-icon">
    <i class="bi bi-plus-circle"></i>
</button>
```

**Archivo:** `src/main/java/com/example/campolibre/Implement/ProductoImplement.java`

Agregar validación en métodos de actualización de stock:
```java
@Override
public ProductoDTO actualizarStock(Long idProducto, Integer nuevoStock) {
    Producto producto = productoRepository.findById(idProducto)
        .orElseThrow(() -> new CustomException("Producto no encontrado"));
    
    // VALIDACIÓN NUEVA
    if ("INACTIVO".equals(producto.getEstado()) || "ELIMINADO".equals(producto.getEstado())) {
        throw new CustomException("No se puede modificar el stock de un producto inactivo o eliminado");
    }
    
    // ... resto del código existente
}
```

---

### **3. Agregar Botón "Eliminar Tienda" para Proveedor (Soft Delete)**

**Problema:**  
El proveedor debe poder eliminar su tienda desde la vista de detalles. Esto es un **Soft Delete** (cambiar estado a `ELIMINADA`) y es **irreversible**. Los productos deben cambiar a `INACTIVO`.

**Solución:**

#### **Backend - Servicio:**
**Archivo:** `src/main/java/com/example/campolibre/Implement/TiendaImplement.java`

Modificar método `eliminarTienda`:
```java
@Override
@Transactional
public void eliminarTienda(Long id) {
    Tienda tienda = tiendaRepository.findById(id)
        .orElseThrow(() -> new CustomException("Tienda no encontrada"));
    
    // Soft Delete: cambiar estado a ELIMINADA
    tienda.setEstado(EstadoTienda.ELIMINADA);
    
    // CASCADA: Cambiar productos a INACTIVO (no ELIMINADO, para mantener historial de pedidos)
    List<Producto> productos = productoRepository.findAllByTiendaId(id);
    for (Producto p : productos) {
        if (!"ELIMINADO".equals(p.getEstado())) {
            p.setEstado("INACTIVO");
        }
    }
    productoRepository.saveAll(productos);
    
    tiendaRepository.save(tienda);
}
```

#### **Backend - Controller:**
**Archivo:** `src/main/java/com/example/campolibre/Controller/TiendaController.java`

Modificar endpoint de eliminación para verificar propiedad:
```java
@PostMapping("/eliminar/{id}")
@PreAuthorize("hasAnyAuthority('ADMINISTRADOR', 'PROVEEDOR')")
public String eliminarTienda(@PathVariable Long id, 
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
    try {
        TiendaDTO tienda = tiendaService.obtenerTiendaPorId(id);
        
        // Verificar que el proveedor sea dueño de la tienda
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
            String email = authentication.getName();
            UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
            
            if (!tienda.getId_usuario().equals(usuario.getId_usuario())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permiso para eliminar esta tienda");
                return "redirect:/tiendas";
            }
        }
        
        tiendaService.eliminarTienda(id);
        redirectAttributes.addFlashAttribute("success", "Tienda eliminada correctamente");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    return "redirect:/tiendas";
}
```

#### **Frontend - Vista de Detalles:**
**Archivo:** `src/main/resources/templates/tienda/view.html`

Agregar botón de eliminar para PROVEEDOR (dueño de la tienda):
```html
<!-- Dentro de la sección de acciones/botones -->
<div sec:authorize="hasAuthority('PROVEEDOR')" 
     th:if="${#authentication.name == tienda.email_usuario}">
    <form th:action="@{/tiendas/eliminar/{id}(id=${tienda.id_tienda})}" 
          method="post" 
          style="display: inline;">
        <button type="submit" 
                class="btn btn-danger"
                onclick="return confirm('⚠️ ADVERTENCIA: Esta acción es IRREVERSIBLE.\n\n¿Está seguro de eliminar esta tienda?\n\nLos productos asociados quedarán inactivos y no podrás recuperar la tienda.')">
            <i class="bi bi-trash"></i> Eliminar Tienda
        </button>
    </form>
</div>
```

---

### **4. Proveedor puede ver Pedidos que contengan sus Productos**

**Problema:**  
Después de que un consumidor realiza una compra, el proveedor debe poder ver los pedidos que incluyan productos de sus tiendas.

**Solución:**

#### **Backend - Repository:**
**Archivo:** `src/main/java/com/example/campolibre/Repository/PedidoRepository.java`

Verificar que existan las queries (ya están implementadas según el código que revisé):
```java
@Query("SELECT DISTINCT p FROM Pedido p JOIN p.items ip WHERE ip.tienda.id_tienda = :idTienda ORDER BY p.fecha_pedido DESC")
List<Pedido> findPedidosConProductosDeTienda(@Param("idTienda") Long idTienda);

@Query("SELECT DISTINCT p FROM Pedido p JOIN p.items ip WHERE ip.tienda.id_tienda = :idTienda AND p.estado = 'PAGADO' ORDER BY p.fecha_pedido DESC")
List<Pedido> findPedidosPagadosDeTienda(@Param("idTienda") Long idTienda);
```

#### **Backend - Service:**
**Archivo:** `src/main/java/com/example/campolibre/Service/PedidoService.java`

Agregar método (si no existe):
```java
List<PedidoDTO> obtenerPedidosPorProveedor(Long idUsuarioProveedor);
```

**Archivo:** `src/main/java/com/example/campolibre/Implement/PedidoImplement.java`

Implementar:
```java
@Override
@Transactional(readOnly = true)
public List<PedidoDTO> obtenerPedidosPorProveedor(Long idUsuarioProveedor) {
    // Obtener todas las tiendas del proveedor
    List<Tienda> tiendas = tiendaRepository.findByUsuarioId(idUsuarioProveedor);
    
    Set<Long> pedidosIds = new HashSet<>();
    List<Pedido> todosPedidos = new ArrayList<>();
    
    // Para cada tienda, obtener pedidos
    for (Tienda tienda : tiendas) {
        List<Pedido> pedidosTienda = pedidoRepository.findPedidosConProductosDeTienda(tienda.getId_tienda());
        for (Pedido p : pedidosTienda) {
            if (!pedidosIds.contains(p.getId_pedido())) {
                pedidosIds.add(p.getId_pedido());
                todosPedidos.add(p);
            }
        }
    }
    
    // Ordenar por fecha descendente
    todosPedidos.sort((p1, p2) -> p2.getFecha_pedido().compareTo(p1.getFecha_pedido()));
    
    return todosPedidos.stream()
        .map(this::convertirADTO)
        .collect(Collectors.toList());
}
```

#### **Backend - Controller:**
**Archivo:** `src/main/java/com/example/campolibre/Controller/HomeController.java` o crear `PedidoController.java`

Agregar endpoint para PROVEEDOR:
```java
@GetMapping("/proveedor/pedidos")
@PreAuthorize("hasAuthority('PROVEEDOR')")
public String verPedidosProveedor(Model model, Authentication authentication) {
    String email = authentication.getName();
    UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
    
    List<PedidoDTO> pedidos = pedidoService.obtenerPedidosPorProveedor(usuario.getId_usuario());
    
    model.addAttribute("pedidos", pedidos);
    model.addAttribute("titulo", "Mis Ventas");
    return "pedido/list_proveedor";
}
```

#### **Frontend - Vista:**
**Archivo:** `src/main/resources/templates/pedido/list_proveedor.html` (CREAR NUEVO)

Crear vista similar a `pedido/list.html` pero adaptada para mostrar:
- Número de pedido
- Fecha
- Cliente (nombre/email)
- Items del pedido QUE PERTENECEN A SUS TIENDAS
- Subtotal de sus productos en ese pedido
- Estado del pedido

**Ejemplo de estructura:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <title>Mis Ventas - Campo Libre</title>
    <link rel="stylesheet" th:href="@{/css/base/main.css}">
    <link rel="stylesheet" th:href="@{/css/components/tables.css}">
</head>
<body>
<div th:replace="~{fragments/navbar :: navbar}"></div>

<main class="main-content">
    <div class="container">
        <h1><i class="bi bi-cart-check"></i> Mis Ventas</h1>
        
        <div class="table-responsive">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Nº Pedido</th>
                        <th>Fecha</th>
                        <th>Cliente</th>
                        <th>Mis Productos</th>
                        <th>Subtotal</th>
                        <th>Estado</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <tr th:each="pedido : ${pedidos}">
                        <td th:text="${pedido.numero_pedido}">PED-001</td>
                        <td th:text="${#temporals.format(pedido.fecha_pedido, 'dd/MM/yyyy HH:mm')}">01/01/2024</td>
                        <td th:text="${pedido.nombre_usuario}">Cliente</td>
                        <td>
                            <!-- Listar solo items de mis tiendas -->
                            <span th:text="${pedido.items.size()}">3</span> productos
                        </td>
                        <td th:text="${'$' + #numbers.formatDecimal(pedido.total, 1, 2)}">$50.000</td>
                        <td>
                            <span class="status-badge" 
                                  th:classappend="${pedido.estado.name() == 'PAGADO'} ? 'active' : 'pending'"
                                  th:text="${pedido.estado.displayName}">Pagado</span>
                        </td>
                        <td>
                            <a th:href="@{/proveedor/pedidos/ver/{id}(id=${pedido.id_pedido})}"
                               class="btn-action btn-view">
                                <i class="bi bi-eye"></i>
                            </a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</main>
</body>
</html>
```

#### **Navegación:**
**Archivo:** `src/main/resources/templates/fragments/navbar.html`

Agregar enlace en el menú del PROVEEDOR:
```html
<li sec:authorize="hasAuthority('PROVEEDOR')">
    <a th:href="@{/proveedor/pedidos}" 
       th:classappend="${#request.requestURI.startsWith('/proveedor/pedidos')} ? 'active' : ''">
        <i class="bi bi-cart-check"></i> Mis Ventas
    </a>
</li>
```

---

### **5. Arreglar Filtros en inventario/panel**

**Problema:**  
Los filtros de estado, stock, subcategoría en la vista `inventario/panel.html` no están funcionando correctamente o no están reflejando correctamente los productos filtrados.

**Solución:**

#### **Backend - Service:**
**Archivo:** `src/main/java/com/example/campolibre/Service/ProductoService.java`

Verificar que existan métodos de filtrado:
```java
List<ProductoDTO> filtrarProductosPorTienda(Long idTienda, String estado, Integer stock, SubcategoriaProducto subcategoria);
```

**Archivo:** `src/main/java/com/example/campolibre/Implement/ProductoImplement.java`

Implementar método de filtrado combinado:
```java
@Override
public List<ProductoDTO> filtrarProductosPorTienda(Long idTienda, String estado, Integer stock, SubcategoriaProducto subcategoria) {
    List<Producto> productos = productoRepository.findAllByTiendaId(idTienda);
    
    // Filtrar por estado
    if (estado != null && !estado.isEmpty() && !"TODOS".equals(estado)) {
        productos = productos.stream()
            .filter(p -> estado.equals(p.getEstado()))
            .collect(Collectors.toList());
    }
    
    // Filtrar por stock
    if (stock != null) {
        if (stock == 0) {
            // Sin stock
            productos = productos.stream()
                .filter(p -> p.getStock() == 0)
                .collect(Collectors.toList());
        } else if (stock == -1) {
            // Stock bajo
            productos = productos.stream()
                .filter(p -> p.tieneStockBajo())
                .collect(Collectors.toList());
        }
    }
    
    // Filtrar por subcategoría
    if (subcategoria != null) {
        productos = productos.stream()
            .filter(p -> subcategoria.equals(p.getSubcategoria()))
            .collect(Collectors.toList());
    }
    
    return productos.stream()
        .map(this::convertirADTO)
        .collect(Collectors.toList());
}
```

#### **Backend - Controller:**
**Archivo:** `src/main/java/com/example/campolibre/Controller/InventarioController.java`

Modificar el endpoint GET para aceptar parámetros de filtro:
```java
@GetMapping("/tienda/{idTienda}")
public String verPanelInventario(@PathVariable Long idTienda,
                                  @RequestParam(required = false) String estado,
                                  @RequestParam(required = false) Integer stock,
                                  @RequestParam(required = false) SubcategoriaProducto subcategoria,
                                  Model model,
                                  Authentication authentication) {
    // ... verificaciones de seguridad existentes ...
    
    TiendaDTO tienda = tiendaService.obtenerTiendaPorId(idTienda);
    List<ProductoDTO> productos;
    
    // Aplicar filtros si existen
    if (estado != null || stock != null || subcategoria != null) {
        productos = productoService.filtrarProductosPorTienda(idTienda, estado, stock, subcategoria);
    } else {
        productos = productoService.obtenerProductosPorTienda(idTienda);
    }
    
    // Calcular resumen de inventario
    ResumenInventarioDTO resumen = productoService.obtenerResumenInventario(idTienda);
    
    model.addAttribute("tienda", tienda);
    model.addAttribute("productos", productos);
    model.addAttribute("resumen", resumen);
    model.addAttribute("estadoFiltro", estado);
    model.addAttribute("stockFiltro", stock);
    model.addAttribute("subcategoriaFiltro", subcategoria);
    
    return "inventario/panel";
}
```

#### **Frontend - Vista:**
**Archivo:** `src/main/resources/templates/inventario/panel.html`

Asegurar que los filtros envíen parámetros correctamente:
```html
<div class="filters-container">
    <form method="get" th:action="@{/inventario/tienda/{id}(id=${tienda.id_tienda})}" 
          class="filters-form">
        
        <!-- Filtro por Estado -->
        <div class="filter-group">
            <label>Estado:</label>
            <select name="estado" onchange="this.form.submit()">
                <option value="">Todos</option>
                <option value="ACTIVO" th:selected="${estadoFiltro == 'ACTIVO'}">Activo</option>
                <option value="INACTIVO" th:selected="${estadoFiltro == 'INACTIVO'}">Inactivo</option>
                <option value="SIN_STOCK" th:selected="${estadoFiltro == 'SIN_STOCK'}">Sin Stock</option>
                <option value="ELIMINADO" th:selected="${estadoFiltro == 'ELIMINADO'}">Eliminado</option>
            </select>
        </div>
        
        <!-- Filtro por Stock -->
        <div class="filter-group">
            <label>Stock:</label>
            <select name="stock" onchange="this.form.submit()">
                <option value="">Todos</option>
                <option value="0" th:selected="${stockFiltro == 0}">Sin Stock</option>
                <option value="-1" th:selected="${stockFiltro == -1}">Stock Bajo</option>
            </select>
        </div>
        
        <!-- Filtro por Subcategoría -->
        <div class="filter-group">
            <label>Subcategoría:</label>
            <select name="subcategoria" onchange="this.form.submit()">
                <option value="">Todas</option>
                <option th:each="sub : ${T(com.example.campolibre.Enum.SubcategoriaProducto).values()}"
                        th:value="${sub}"
                        th:text="${sub.displayName}"
                        th:selected="${subcategoriaFiltro == sub}">
                </option>
            </select>
        </div>
        
        <!-- Botón Limpiar Filtros -->
        <a th:href="@{/inventario/tienda/{id}(id=${tienda.id_tienda})}" 
           class="btn btn-secondary">
            <i class="bi bi-x-circle"></i> Limpiar
        </a>
    </form>
</div>
```

---

### **6. Arreglar Navegación y Filtros - Excluir Tiendas/Productos Eliminados o Inactivos**

**Problema:**  
En vistas públicas y de consumidor, están apareciendo tiendas/productos con estado `ELIMINADO` o `INACTIVO`.

**Solución:**

#### **Reglas de Visibilidad:**
1. **Consumidor:**
    - Solo ve tiendas con estado `ACTIVA`
    - Solo ve productos con estado `ACTIVO`

2. **Proveedor:**
    - Ve sus tiendas en todos los estados (ACTIVA, INACTIVA, ELIMINADA)
    - Ve sus productos en todos los estados (ACTIVO, INACTIVO, SIN_STOCK, ELIMINADO)

3. **Administrador:**
    - Ve todo

#### **Backend - Verificar Queries:**

**Archivo:** `src/main/java/com/example/campolibre/Repository/TiendaRepository.java`

Verificar que existan queries específicas:
```java
@Query("SELECT t FROM Tienda t WHERE t.estado = 'ACTIVA'")
List<Tienda> findAllActive();

@Query("SELECT t FROM Tienda t WHERE t.usuario.id_usuario = :idUsuario")
List<Tienda> findByUsuarioId(@Param("idUsuario") Long idUsuario);
```

**Archivo:** `src/main/java/com/example/campolibre/Repository/ProductoRepository.java`

Verificar queries (ya revisadas, están correctas):
```java
@Query("SELECT p FROM Producto p WHERE p.estado = 'ACTIVO'")
List<Producto> findAllActive();

@Query("SELECT p FROM Producto p WHERE p.tienda.id_tienda = :idTienda AND p.estado = 'ACTIVO'")
List<Producto> findByTiendaIdAndEstadoActivo(@Param("idTienda") Long idTienda);
```

#### **Backend - Controllers:**

**Archivo:** `src/main/java/com/example/campolibre/Controller/TiendaController.java`

Verificar que el método `listarTiendas()` use la lógica correcta:
```java
@GetMapping
public String listarTiendas(Model model, Authentication authentication) {
    List<TiendaDTO> tiendas;
    String tipoLista;

    if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
        tiendas = tiendaService.obtenerTodasLasTiendas();
        tipoLista = "todas";
    } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
        tiendas = tiendaService.obtenerTiendasPorUsuario(usuario.getId_usuario());
        tipoLista = "mis_tiendas";
    } else {
        // CONSUMIDOR - Solo tiendas ACTIVAS
        tiendas = tiendaService.obtenerTiendasActivas();
        tipoLista = "activas";
    }

    model.addAttribute("tiendas", tiendas);
    model.addAttribute("tipoLista", tipoLista);
    return "tienda/list";
}
```

**Archivo:** `src/main/java/com/example/campolibre/Controller/ProductoController.java`

Verificar método `listarProductos()`:
```java
@GetMapping
public String listarProductos(
        @RequestParam(value = "idTienda", required = false) Long idTienda,
        Model model,
        Authentication authentication) {
    
    List<ProductoDTO> productos;
    
    // Lógica de filtrado por rol
    if (authentication == null || 
        authentication.getAuthorities().contains(new SimpleGrantedAuthority("CONSUMIDOR"))) {
        // Solo productos ACTIVOS
        productos = (idTienda != null)
                ? productoService.obtenerProductosActivosPorTienda(idTienda)
                : productoService.obtenerProductosActivos();
    } else {
        // ADMIN o PROVEEDOR ven todos
        productos = (idTienda != null)
                ? productoService.obtenerProductosPorTienda(idTienda)
                : productoService.obtenerTodosLosProductos();
    }
    
    // ... resto del código
}
```

#### **Crear método en Service si falta:**

**Archivo:** `src/main/java/com/example/campolibre/Service/ProductoService.java`
```java
List<ProductoDTO> obtenerProductosActivosPorTienda(Long idTienda);
```

**Archivo:** `src/main/java/com/example/campolibre/Implement/ProductoImplement.java`
```java
@Override
public List<ProductoDTO> obtenerProductosActivosPorTienda(Long idTienda) {
    List<Producto> productos = productoRepository.findByTiendaIdAndEstadoActivo(idTienda);
    return productos.stream()
        .map(this::convertirADTO)
        .collect(Collectors.toList());
}
```

#### **Frontend - Filtrado en Vistas:**

**Archivo:** `src/main/resources/templates/producto/list.html`

Verificar que solo se muestren productos activos para consumidor:
```html
<!-- Si es necesario, agregar filtro adicional en Thymeleaf -->
<tr th:each="producto : ${productos}" 
    th:if="${producto.estado == 'ACTIVO'} or ${#authorization.expression('hasAnyAuthority(''ADMINISTRADOR'', ''PROVEEDOR'')')}">
    <!-- ... contenido de la fila ... -->
</tr>
```

**Archivo:** `src/main/resources/templates/tienda/list.html`

Similar para tiendas:
```html
<tr th:each="tienda : ${tiendas}" 
    th:if="${tienda.estado.name() == 'ACTIVA'} or ${#authorization.expression('hasAnyAuthority(''ADMINISTRADOR'', ''PROVEEDOR'')')}">
    <!-- ... contenido de la fila ... -->
</tr>
```

---

## **VALIDACIONES ADICIONALES REQUERIDAS**

### **En Compras de Productos:**
**Archivo:** `src/main/java/com/example/campolibre/Implement/CarritoImplement.java`

Verificar que el método `agregarItem()` valide estado del producto y tienda:
```java
@Override
@Transactional
public ItemCarritoDTO agregarItem(Long idUsuario, Long idProducto, Integer cantidad) {
    // Validaciones existentes...
    
    Producto producto = productoRepository.findById(idProducto)
        .orElseThrow(() -> new CustomException("Producto no encontrado"));
    
    // NUEVAS VALIDACIONES
    if (!"ACTIVO".equals(producto.getEstado())) {
        throw new CustomException("Este producto no está disponible para la compra");
    }
    
    Tienda tienda = producto.getTienda();
    if (tienda.getEstado() != EstadoTienda.ACTIVA) {
        throw new CustomException("La tienda de este producto no está activa");
    }
    
    // ... resto del código existente
}
```

### **En Actualización de Stock:**
Ya implementado anteriormente en el punto 2, verificar que esté presente la validación:
```java
if ("INACTIVO".equals(producto.getEstado()) || "ELIMINADO".equals(producto.getEstado())) {
    throw new CustomException("No se puede modificar el stock de un producto inactivo o eliminado");
}
```

---

## **RESUMEN DE ARCHIVOS A MODIFICAR**

### **Backend - Java:**
1. `TiendaService.java` - Agregar métodos activar/inactivar
2. `TiendaImplement.java` - Implementar métodos activar/inactivar/eliminar con cascada
3. `TiendaController.java` - Agregar endpoints activar/inactivar
4. `ProductoService.java` - Agregar método filtrado combinado
5. `ProductoImplement.java` - Implementar filtrado + validaciones estado
6. `PedidoService.java` - Agregar método obtenerPedidosPorProveedor
7. `PedidoImplement.java` - Implementar método
8. `CarritoImplement.java` - Validaciones de estado en agregarItem
9. `HomeController.java` o crear `PedidoProveedorController.java` - Endpoint ventas

### **Backend - Repository:**
10. `TiendaRepository.java` - Verificar query findByEstado
11. `ProductoRepository.java` - Verificar queries de filtrado (ya están)

### **Frontend - HTML:**
12. `tienda/list.html` - Quitar botón "Todas" para Proveedor + Botones activar/inactivar
13. `tienda/view.html` - Botón eliminar para Proveedor
14. `inventario/panel.html` - Arreglar filtros + Deshabilitar botones stock si inactivo
15. `producto/list.html` - Verificar filtrado visual
16. `pedido/list_proveedor.html` - CREAR NUEVA VISTA
17. `navbar.html` - Agregar enlace "Mis Ventas" para Proveedor

---

## **ORDEN DE IMPLEMENTACIÓN SUGERIDO**

1. **Backend primero:**
    - Métodos en Services e Implements
    - Endpoints en Controllers
    - Queries en Repositories si faltan

2. **Validaciones:**
    - Agregar validaciones de estado en operaciones críticas

3. **Frontend:**
    - Modificar vistas existentes
    - Crear nueva vista de pedidos para proveedor
    - Actualizar navegación

4. **Pruebas:**
    - Probar cada funcionalidad individualmente
    - Verificar cascadas de estado
    - Validar permisos por rol

---

## **NOTAS IMPORTANTES**

- ⚠️ **Transacciones:** Usar `@Transactional` en operaciones que modifiquen múltiples entidades
- ⚠️ **Seguridad:** Verificar que los endpoints tengan `@PreAuthorize` apropiados
- ⚠️ **Cascada:** Al cambiar estado de tienda, SIEMPRE actualizar productos
- ⚠️ **Soft Delete:** NUNCA hacer delete físico, siempre cambiar estado
- ⚠️ **Historial:** Al inactivar productos por eliminación de tienda, usar INACTIVO (no ELIMINADO) para mantener historial de pedidos

---

## **VERIFICACIÓN POST-IMPLEMENTACIÓN**

Después de implementar, verificar:

✅ Proveedor NO ve botón "Todas" en tienda/list  
✅ Admin puede activar/inactivar tiendas  
✅ Al inactivar tienda, productos cambian a INACTIVO  
✅ Productos inactivos no se muestran a Consumidor  
✅ Productos inactivos no permiten modificar stock  
✅ Proveedor puede eliminar su tienda (soft delete)  
✅ Proveedor ve pedidos con sus productos  
✅ Filtros en inventario funcionan correctamente  
✅ Navegación excluye elementos eliminados/inactivos para Consumidor  
✅ Validaciones impiden comprar productos inactivos

---

**Fin del documento de instrucciones**