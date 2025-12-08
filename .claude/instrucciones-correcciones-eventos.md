# Instrucciones de Corrección: Módulo Eventos
## Proyecto Campo Libre - Defectos encontrados en Pruebas

---

## **CONTEXTO DEL PROYECTO**

**Nombre:** Campo Libre  
**Tipo:** Aplicación web tipo Marketplace agrícola  
**Stack:** Spring Boot + Thymeleaf + MySQL  
**Arquitectura:** MVC

### **Roles del Sistema:**
- **Administrador:** Gestión completa de eventos, aprobar/rechazar, ver reportes
- **Consumidor:** Ver eventos, guardar intención de asistencia
- **Proveedor:** Inscribirse a eventos pagando cupo, ver sus inscripciones, cancelar inscripciones

### **Estados de Evento:**
- `BORRADOR` - En creación por admin
- `PUBLICADO` - Visible para proveedores/consumidores
- `EN_CURSO` - Evento activo (día del evento)
- `FINALIZADO` - Evento terminado
- `CANCELADO` - Evento cancelado

### **Estados de Inscripción (EstadoCupo):**
- `PENDIENTE` - Cupo reservado, esperando pago
- `CONFIRMADO` - Pago realizado, cupo asegurado
- `CANCELADO` - Inscripción cancelada

### **Estados de Pago (EstadoPago):**
- `PENDIENTE` - Pago iniciado
- `EXITOSO` - Pago confirmado
- `FALLIDO` - Pago rechazado
- `REEMBOLSADO` - (NUEVO) Pago reembolsado por cancelación

---

## **DEFECTOS IDENTIFICADOS Y SOLUCIONES**

### **GRUPO 1: RESTRICCIONES DE INSCRIPCIÓN**

#### **1. No permitir inscripción a eventos Cancelados, Finalizados o En Curso**

**Problema:**  
Actualmente un proveedor puede intentar inscribirse a eventos que no están en estado PUBLICADO.

**Solución:**

**Archivo:** `src/main/java/com/example/campolibre/Implement/InscripcionProveedorImplement.java`

Modificar método `solicitarCupo()` para agregar validaciones adicionales:

```java
@Override
public InscripcionProveedorDTO solicitarCupo(Long idProveedor, Long idEvento) {
    // ... código existente de verificación de entidades ...
    
    Usuario proveedor = usuarioRepository.findById(idProveedor)
        .orElseThrow(() -> new CustomException("Proveedor no encontrado."));
    
    Evento evento = eventoRepository.findById(idEvento)
        .orElseThrow(() -> new CustomException("Evento no encontrado."));
    
    // ✅ VALIDACIÓN MEJORADA: Solo permitir inscripción a eventos PUBLICADOS
    if (evento.getEstado() != EstadoEvento.PUBLICADO) {
        String mensajeEstado = switch (evento.getEstado()) {
            case BORRADOR -> "Este evento aún no ha sido publicado.";
            case CANCELADO -> "Este evento ha sido cancelado y no acepta inscripciones.";
            case FINALIZADO -> "Este evento ya finalizó y no acepta inscripciones.";
            case EN_CURSO -> "Este evento ya está en curso y no acepta nuevas inscripciones.";
            default -> "Este evento no está disponible para inscripciones.";
        };
        throw new CustomException(mensajeEstado);
    }
    
    // ... resto del código existente ...
}
```

**Archivo:** `src/main/resources/templates/evento/view.html`

Modificar botón de inscripción para deshabilitar según estado:

```html
<!-- Botón Inscribirse - Solo si evento está PUBLICADO -->
<button th:if="${!yaSeInscribio and !isPagoPendiente and evento.estado.name() == 'PUBLICADO'}"
        onclick="confirmarInscripcion()"
        class="btn btn-success btn-lg w-100"
        th:disabled="${!hayCupos}">
    <i class="bi bi-calendar-check"></i>
    <span th:if="${hayCupos}">Inscribirse al Evento</span>
    <span th:unless="${hayCupos}">Sin Cupos Disponibles</span>
</button>

<!-- Mensaje si evento NO está publicado -->
<div th:if="${evento.estado.name() != 'PUBLICADO'}" 
     class="alert alert-warning">
    <i class="bi bi-exclamation-triangle"></i>
    <span th:switch="${evento.estado.name()}">
        <span th:case="'CANCELADO'">Este evento ha sido cancelado.</span>
        <span th:case="'FINALIZADO'">Este evento ya finalizó.</span>
        <span th:case="'EN_CURSO'">Este evento ya está en curso y no acepta nuevas inscripciones.</span>
        <span th:case="'BORRADOR'">Este evento aún no ha sido publicado.</span>
    </span>
</div>
```

---

#### **2. Botón Volver en evento/view**

**Problema:**  
Falta un botón "Volver" en la vista de detalles del evento.

**Solución:**

**Archivo:** `src/main/resources/templates/evento/view.html`

Agregar botón "Volver" al final de la página, antes del cierre de `</main>`:

```html
<!-- Agregar justo antes del cierre </main> -->
<div class="row mt-4">
    <div class="col-12">
        <a th:href="@{/eventos}" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left"></i> Volver a Eventos
        </a>
        
        <!-- Botón específico para proveedores que vienen de mis-inscripciones -->
        <a sec:authorize="hasAuthority('PROVEEDOR')"
           th:href="@{/eventos/mis-inscripciones}" 
           class="btn btn-outline-primary ms-2">
            <i class="bi bi-calendar-check"></i> Mis Inscripciones
        </a>
        
        <!-- Botón específico para admin -->
        <a sec:authorize="hasAuthority('ADMINISTRADOR')"
           th:href="@{/eventos/admin/todos}" 
           class="btn btn-outline-primary ms-2">
            <i class="bi bi-gear"></i> Gestión de Eventos
        </a>
    </div>
</div>
```

---

### **GRUPO 2: FORMULARIO DE EDICIÓN DE EVENTOS**

#### **3. Arreglar formulario evento/edit - Fecha y Hora no se cargan**

**Problema:**  
El formulario de edición no precarga los valores de `fecha_evento` y `hora_evento` del evento existente.

**Solución:**

**Archivo:** `src/main/java/com/example/campolibre/Controller/EventoController.java`

Modificar método `mostrarFormularioEdicion()`:

```java
@GetMapping("/editar/{id}")
public String mostrarFormularioEdicion(@PathVariable Long id, 
                                       Model model, 
                                       Authentication authentication) {
    if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ADMINISTRADOR"))) {
        return "redirect:/eventos";
    }
    
    // Obtener evento
    EventoDTO eventoDTO = eventoService.obtenerEventoPorId(id);
    
    // ✅ CREAR EventoCreacionDTO y copiar TODOS los campos
    EventoCreacionDTO eventoForm = new EventoCreacionDTO();
    eventoForm.setId_evento(eventoDTO.getId_evento());
    eventoForm.setNombre(eventoDTO.getNombre());
    eventoForm.setDescripcion(eventoDTO.getDescripcion());
    eventoForm.setUbicacion(eventoDTO.getUbicacion());
    eventoForm.setDireccionCompleta(eventoDTO.getDireccionCompleta());
    
    // ✅ IMPORTANTE: Asegurar que fecha y hora se pasen
    eventoForm.setFecha_evento(eventoDTO.getFecha_evento());
    eventoForm.setHora_evento(eventoDTO.getHora_evento());
    
    eventoForm.setTipo_evento(eventoDTO.getTipo_evento());
    eventoForm.setId_patrocinador(eventoDTO.getId_patrocinador());
    eventoForm.setCuposMaximosProveedor(eventoDTO.getCuposMaximosProveedor());
    eventoForm.setCuposOcupados(eventoDTO.getCuposOcupados());
    eventoForm.setCostoEspacio(eventoDTO.getCostoEspacio());
    eventoForm.setTerminosCondiciones(eventoDTO.getTerminosCondiciones());
    eventoForm.setImagen_evento(eventoDTO.getImagen_evento());
    
    // Cargar patrocinadores
    List<PatrocinadorDTO> patrocinadores = patrocinadorService.obtenerTodosLosPatrocinadores();
    
    model.addAttribute("evento", eventoForm);
    model.addAttribute("tiposEvento", TipoEvento.values());
    model.addAttribute("patrocinadores", patrocinadores);
    
    return "evento/edit";
}
```

**Archivo:** `src/main/resources/templates/evento/edit.html`

Verificar que los campos usen `th:field` correctamente:

```html
<!-- Fecha del Evento -->
<div class="col-md-6 mb-3">
    <label for="fecha_evento" class="form-label">
        <i class="bi bi-calendar3 text-info"></i>
        Fecha <span class="text-danger">*</span>
    </label>
    <input type="date"
           class="form-control"
           id="fecha_evento"
           th:field="*{fecha_evento}"
           required>
    <div class="invalid-feedback">
        La fecha es obligatoria
    </div>
</div>

<!-- Hora del Evento -->
<div class="col-md-6 mb-3">
    <label for="hora_evento" class="form-label">
        <i class="bi bi-clock text-info"></i>
        Hora <span class="text-danger">*</span>
    </label>
    <input type="time"
           class="form-control"
           id="hora_evento"
           th:field="*{hora_evento}"
           required>
    <div class="invalid-feedback">
        La hora es obligatoria
    </div>
</div>
```

---

### **GRUPO 3: CANCELACIÓN Y REEMBOLSOS**

#### **4. Arreglar Botón "Cancelar" en mis-inscripciones + Lógica de Reembolso**

**Problema:**  
El botón cancelar existe pero necesita lógica completa de reembolso con cálculo de porcentaje según tiempo restante.

**Solución:**

**Archivo:** `src/main/java/com/example/campolibre/Enum/EstadoPago.java`

Agregar nuevo estado:

```java
package com.example.campolibre.Enum;

public enum EstadoPago {
    PENDIENTE,
    EXITOSO,
    FALLIDO,
    REEMBOLSADO  // ✅ NUEVO ESTADO
}
```

**Archivo:** `src/main/java/com/example/campolibre/Service/InscripcionProveedorService.java`

Agregar método:

```java
// Cancelar inscripción con cálculo de reembolso
InscripcionProveedorDTO cancelarInscripcionConReembolso(Long idInscripcion);
```

**Archivo:** `src/main/java/com/example/campolibre/Implement/InscripcionProveedorImplement.java`

Modificar método `cancelarInscripcion()` existente:

```java
@Override
@Transactional
public void cancelarInscripcion(Long idInscripcion, String motivo) {
    InscripcionProveedor inscripcion = inscripcionRepository.findById(idInscripcion)
        .orElseThrow(() -> new CustomException("Inscripción no encontrada"));
    
    // Validar que esté confirmada (solo se pueden cancelar inscripciones pagadas)
    if (inscripcion.getEstadoCupo() != EstadoCupo.CONFIRMADO) {
        throw new CustomException("Solo se pueden cancelar inscripciones confirmadas");
    }
    
    Evento evento = inscripcion.getEvento();
    
    // Validar que el evento no haya finalizado o esté en curso
    if (evento.getEstado() == EstadoEvento.FINALIZADO) {
        throw new CustomException("No se pueden cancelar inscripciones de eventos finalizados");
    }
    
    if (evento.getEstado() == EstadoEvento.EN_CURSO) {
        throw new CustomException("No se pueden cancelar inscripciones de eventos en curso");
    }
    
    // ✅ CALCULAR PORCENTAJE DE REEMBOLSO
    Double porcentajeReembolso = calcularPorcentajeReembolso(evento);
    Double montoReembolso = inscripcion.getCostoPagado() * porcentajeReembolso;
    
    // Cambiar estado de inscripción
    inscripcion.cancelarInscripcion();
    
    // Liberar cupo
    evento.setCuposOcupados(evento.getCuposOcupados() - 1);
    eventoRepository.save(evento);
    
    // Actualizar estado del pago asociado
    if (inscripcion.getPagoEvento() != null) {
        PagoEvento pago = inscripcion.getPagoEvento();
        pago.setEstado(EstadoPago.REEMBOLSADO);
        pago.setObservaciones(String.format(
            "Cancelado por proveedor. Reembolso: %.0f%% ($%,.0f). Motivo: %s",
            porcentajeReembolso * 100,
            montoReembolso,
            motivo
        ));
        pagoEventoRepository.save(pago);
    }
    
    inscripcionRepository.save(inscripcion);
    
    // Log para seguimiento
    System.out.println(String.format(
        "✅ Inscripción %d cancelada. Reembolso: %.0f%% ($%,.0f)",
        idInscripcion,
        porcentajeReembolso * 100,
        montoReembolso
    ));
}

/**
 * Calcula el porcentaje de reembolso según tiempo restante hasta el evento
 */
private Double calcularPorcentajeReembolso(Evento evento) {
    LocalDateTime ahora = LocalDateTime.now();
    LocalDateTime fechaEvento = LocalDateTime.of(evento.getFecha_evento(), evento.getHora_evento());
    
    long horasRestantes = java.time.Duration.between(ahora, fechaEvento).toHours();
    
    // Más de 48 horas (2 días): 70% de reembolso
    if (horasRestantes >= 48) {
        return 0.70;
    }
    // Entre 24 y 48 horas: 50% de reembolso
    else if (horasRestantes >= 24) {
        return 0.50;
    }
    // Menos de 24 horas: Sin reembolso
    else {
        return 0.0;
    }
}
```

**Archivo:** `src/main/java/com/example/campolibre/Controller/EventoController.java`

Modificar endpoint de cancelación:

```java
@PostMapping("/cancelar-inscripcion/{idInscripcion}")
@PreAuthorize("hasAuthority('PROVEEDOR')")
public String cancelarInscripcion(@PathVariable Long idInscripcion,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
    try {
        String email = authentication.getName();
        UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
        
        // Obtener inscripción para validar propiedad
        InscripcionProveedorDTO inscripcion = inscripcionProveedorService.obtenerInscripcionPorId(idInscripcion);
        
        if (!inscripcion.getId_proveedor().equals(usuario.getId_usuario())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para cancelar esta inscripción");
            return "redirect:/eventos/mis-inscripciones";
        }
        
        // Cancelar con reembolso
        inscripcionProveedorService.cancelarInscripcion(idInscripcion, "Cancelado por el proveedor");
        
        // ✅ MENSAJE DE ÉXITO CON INFORMACIÓN DE REEMBOLSO
        redirectAttributes.addFlashAttribute("success", 
            "Inscripción cancelada exitosamente. El proceso de reembolso se llevará a cabo mediante correo electrónico en los próximos 5-7 días hábiles.");
        
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Error al cancelar: " + e.getMessage());
    }
    
    return "redirect:/eventos/mis-inscripciones";
}
```

**Archivo:** `src/main/resources/templates/evento/mis-inscripciones.html`

Modificar script de confirmación de cancelación:

```html
<script>
function confirmarCancelacion(button) {
    const inscripcionId = button.dataset.inscripcionId;
    const eventoNombre = button.dataset.eventoNombre;
    
    const confirmar = confirm(
        `¿Estás seguro de cancelar tu inscripción a "${eventoNombre}"?\n\n` +
        `⚠️ Información importante:\n` +
        `• Más de 48 horas antes: Reembolso del 70%\n` +
        `• Entre 24-48 horas: Reembolso del 50%\n` +
        `• Menos de 24 horas: Sin reembolso\n\n` +
        `El proceso de reembolso se gestionará por correo electrónico.`
    );
    
    if (confirmar) {
        // Crear formulario y enviar
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = `/eventos/cancelar-inscripcion/${inscripcionId}`;
        document.body.appendChild(form);
        form.submit();
    }
}
</script>
```

---

### **GRUPO 4: SISTEMA DE PAGOS - PSE Y FORMULARIOS**

#### **5. Diseñar Formulario PSE con Entidades Bancarias**

**Problema:**  
Actualmente el formulario de pago no tiene campos específicos para PSE ni selección de entidades bancarias.

**Solución:**

**Archivo:** `src/main/java/com/example/campolibre/Enum/EntidadBancaria.java` (CREAR NUEVO)

```java
package com.example.campolibre.Enum;

public enum EntidadBancaria {
    // Para Tarjetas de Crédito/Débito
    BANCOLOMBIA("Bancolombia"),
    DAVIVIENDA("Davivienda"),
    BBVA("BBVA Colombia"),
    
    // Para PSE
    NEQUI("Nequi"),
    DAVIPLATA("DaviPlata");
    
    private final String displayName;
    
    EntidadBancaria(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    // Métodos helper
    public boolean esParaTarjeta() {
        return this == BANCOLOMBIA || this == DAVIVIENDA || this == BBVA;
    }
    
    public boolean esParaPSE() {
        return this == NEQUI || this == DAVIPLATA;
    }
}
```

**Archivo:** `src/main/java/com/example/campolibre/Enum/TipoPersonaPSE.java` (CREAR NUEVO)

```java
package com.example.campolibre.Enum;

public enum TipoPersonaPSE {
    NATURAL("Persona Natural"),
    JURIDICA("Persona Jurídica");
    
    private final String displayName;
    
    TipoPersonaPSE(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

**Archivo:** `src/main/java/com/example/campolibre/Entity/PagoEvento.java`

Agregar campos nuevos:

```java
// Agregar estos campos a la entidad existente

@Enumerated(EnumType.STRING)
@Column(name = "entidad_bancaria")
private EntidadBancaria entidadBancaria;

@Enumerated(EnumType.STRING)
@Column(name = "tipo_persona_pse")
private TipoPersonaPSE tipoPersonaPSE;

@Column(name = "numero_documento_pse", length = 20)
private String numeroDocumentoPSE;
```

**Archivo:** `src/main/java/com/example/campolibre/DTO/PagoEventoCreacionDTO.java`

Agregar campos:

```java
// Agregar a DTO existente
private EntidadBancaria entidadBancaria;
private TipoPersonaPSE tipoPersonaPSE;
private String numeroDocumentoPSE;
```

**Archivo:** `src/main/resources/templates/pago-evento/checkout.html`

Reemplazar el formulario de pago con uno dinámico:

```html
<!-- Reemplazar el formulario existente -->
<form th:action="@{/pagos-eventos/procesar/{id}(id=${inscripcion.id_inscripcion})}"
      method="post"
      id="formPago"
      class="needs-validation"
      novalidate>

    <!-- Selección de Método de Pago -->
    <div class="mb-4">
        <label class="form-label fw-bold">
            <i class="bi bi-credit-card"></i>
            Método de Pago <span class="text-danger">*</span>
        </label>
        
        <div class="row g-3">
            <!-- Tarjeta de Crédito -->
            <div class="col-md-4">
                <input type="radio" 
                       class="btn-check" 
                       name="metodoPago" 
                       id="metodo_credito" 
                       value="TARJETA_CREDITO"
                       onchange="mostrarCamposMetodo('TARJETA')"
                       required>
                <label class="btn btn-outline-primary w-100 p-3" for="metodo_credito">
                    <i class="bi bi-credit-card fs-3"></i>
                    <div class="mt-2">Tarjeta de Crédito</div>
                </label>
            </div>
            
            <!-- Tarjeta de Débito -->
            <div class="col-md-4">
                <input type="radio" 
                       class="btn-check" 
                       name="metodoPago" 
                       id="metodo_debito" 
                       value="TARJETA_DEBITO"
                       onchange="mostrarCamposMetodo('TARJETA')"
                       required>
                <label class="btn btn-outline-success w-100 p-3" for="metodo_debito">
                    <i class="bi bi-wallet2 fs-3"></i>
                    <div class="mt-2">Tarjeta de Débito</div>
                </label>
            </div>
            
            <!-- PSE -->
            <div class="col-md-4">
                <input type="radio" 
                       class="btn-check" 
                       name="metodoPago" 
                       id="metodo_pse" 
                       value="PSE"
                       onchange="mostrarCamposMetodo('PSE')"
                       required>
                <label class="btn btn-outline-info w-100 p-3" for="metodo_pse">
                    <i class="bi bi-bank fs-3"></i>
                    <div class="mt-2">PSE</div>
                </label>
            </div>
        </div>
        
        <div class="invalid-feedback">
            Selecciona un método de pago
        </div>
    </div>

    <!-- Campos para TARJETA (Crédito/Débito) -->
    <div id="campos-tarjeta" style="display: none;">
        <div class="card bg-light mb-3">
            <div class="card-body">
                <h6 class="card-title">
                    <i class="bi bi-bank"></i> Información Bancaria
                </h6>
                
                <!-- Entidad Bancaria para Tarjeta -->
                <div class="mb-3">
                    <label class="form-label">Entidad Bancaria</label>
                    <select class="form-select" 
                            name="entidadBancaria" 
                            id="entidad-tarjeta">
                        <option value="">-- Seleccione --</option>
                        <option value="BANCOLOMBIA">Bancolombia</option>
                        <option value="DAVIVIENDA">Davivienda</option>
                        <option value="BBVA">BBVA Colombia</option>
                    </select>
                </div>
                
                <!-- Número de Tarjeta -->
                <div class="mb-3">
                    <label class="form-label">Número de Tarjeta</label>
                    <input type="text" 
                           class="form-control" 
                           placeholder="1234 5678 9012 3456"
                           maxlength="19"
                           id="numero-tarjeta">
                </div>
                
                <div class="row">
                    <!-- Fecha Vencimiento -->
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Fecha de Vencimiento</label>
                        <input type="text" 
                               class="form-control" 
                               placeholder="MM/AA"
                               maxlength="5"
                               id="fecha-vencimiento">
                    </div>
                    
                    <!-- CVV -->
                    <div class="col-md-6 mb-3">
                        <label class="form-label">CVV</label>
                        <input type="text" 
                               class="form-control" 
                               placeholder="123"
                               maxlength="4"
                               id="cvv-tarjeta">
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Campos para PSE -->
    <div id="campos-pse" style="display: none;">
        <div class="card bg-light mb-3">
            <div class="card-body">
                <h6 class="card-title">
                    <i class="bi bi-bank"></i> Información PSE
                </h6>
                
                <!-- Tipo de Persona -->
                <div class="mb-3">
                    <label class="form-label">Tipo de Persona</label>
                    <select class="form-select" 
                            name="tipoPersonaPSE" 
                            id="tipo-persona-pse">
                        <option value="">-- Seleccione --</option>
                        <option value="NATURAL">Persona Natural</option>
                        <option value="JURIDICA">Persona Jurídica</option>
                    </select>
                </div>
                
                <!-- Entidad Bancaria para PSE -->
                <div class="mb-3">
                    <label class="form-label">Entidad Financiera</label>
                    <select class="form-select" 
                            name="entidadBancaria" 
                            id="entidad-pse">
                        <option value="">-- Seleccione --</option>
                        <option value="NEQUI">Nequi</option>
                        <option value="DAVIPLATA">DaviPlata</option>
                    </select>
                </div>
                
                <!-- Número de Documento -->
                <div class="mb-3">
                    <label class="form-label">Número de Documento</label>
                    <input type="text" 
                           class="form-control" 
                           name="numeroDocumentoPSE"
                           placeholder="Ingrese su número de documento"
                           id="documento-pse">
                </div>
            </div>
        </div>
    </div>

    <!-- Términos y Condiciones -->
    <div class="form-check mb-4">
        <input class="form-check-input" 
               type="checkbox" 
               id="terminos" 
               required
               onchange="validarTerminos()">
        <label class="form-check-label" for="terminos">
            Acepto los 
            <a href="#" data-bs-toggle="modal" data-bs-target="#modalTerminos">
                términos y condiciones
            </a>
            <span class="text-danger">*</span>
        </label>
        <div class="invalid-feedback">
            Debes aceptar los términos y condiciones
        </div>
    </div>

    <!-- Botón de Pago -->
    <div class="d-grid gap-2">
        <button type="submit"
                class="btn btn-success btn-lg"
                id="btnPagar"
                disabled>
            <i class="bi bi-lock"></i>
            Pagar $<span th:text="${#numbers.formatDecimal(inscripcion.costoPagado, 0, 'COMMA', 0, 'POINT')}">50,000</span>
        </button>
        <a th:href="@{/eventos/ver/{id}(id=${inscripcion.id_evento})}"
           class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left"></i> Cancelar
        </a>
    </div>
</form>

<!-- Script para manejar campos dinámicos -->
<script>
function mostrarCamposMetodo(tipo) {
    const camposTarjeta = document.getElementById('campos-tarjeta');
    const camposPSE = document.getElementById('campos-pse');
    
    if (tipo === 'TARJETA') {
        camposTarjeta.style.display = 'block';
        camposPSE.style.display = 'none';
        
        // Hacer campos de tarjeta requeridos
        document.getElementById('entidad-tarjeta').required = true;
        document.getElementById('numero-tarjeta').required = true;
        document.getElementById('fecha-vencimiento').required = true;
        document.getElementById('cvv-tarjeta').required = true;
        
        // Remover requerido de PSE
        document.getElementById('tipo-persona-pse').required = false;
        document.getElementById('entidad-pse').required = false;
        document.getElementById('documento-pse').required = false;
    } else if (tipo === 'PSE') {
        camposTarjeta.style.display = 'none';
        camposPSE.style.display = 'block';
        
        // Hacer campos PSE requeridos
        document.getElementById('tipo-persona-pse').required = true;
        document.getElementById('entidad-pse').required = true;
        document.getElementById('documento-pse').required = true;
        
        // Remover requerido de tarjeta
        document.getElementById('entidad-tarjeta').required = false;
        document.getElementById('numero-tarjeta').required = false;
        document.getElementById('fecha-vencimiento').required = false;
        document.getElementById('cvv-tarjeta').required = false;
    }
}

function validarTerminos() {
    const terminosCheck = document.getElementById('terminos');
    const btnPagar = document.getElementById('btnPagar');
    
    // ✅ Habilitar/deshabilitar botón según checkbox
    btnPagar.disabled = !terminosCheck.checked;
}

// Inicializar botón como deshabilitado
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('btnPagar').disabled = true;
});
</script>
```

---

#### **6. Bloquear Botón "Pagar" si términos no están aceptados**

**Solución:**  
Ya implementado en el código anterior del formulario PSE. El botón `btnPagar` se habilita/deshabilita dinámicamente con la función `validarTerminos()`.

---

### **GRUPO 5: GESTIÓN DE CUPOS E INSCRIPCIONES**

#### **7. No bajar cupos hasta que se pague la inscripción**

**Problema:**  
Actualmente al solicitar cupo, se baja inmediatamente el cupo disponible.

**Solución:**

**Archivo:** `src/main/java/com/example/campolibre/Implement/InscripcionProveedorImplement.java`

Modificar método `solicitarCupo()` para NO descontar cupo:

```java
@Override
public InscripcionProveedorDTO solicitarCupo(Long idProveedor, Long idEvento) {
    // ... validaciones existentes ...
    
    // 4. Crear la inscripción PENDIENTE
    InscripcionProveedor inscripcion = new InscripcionProveedor();
    inscripcion.setProveedor(proveedor);
    inscripcion.setEvento(evento);
    inscripcion.setEstadoCupo(EstadoCupo.PENDIENTE);
    inscripcion.setCostoPagado(evento.getCostoEspacio());
    
    // ✅ NO DESCONTAR CUPO AQUÍ
    // El cupo se descuenta solo cuando el pago es EXITOSO
    
    InscripcionProveedor nuevaInscripcion = inscripcionRepository.save(inscripcion);
    
    return mapearConDatosVisualizacion(nuevaInscripcion);
}
```

Modificar método `confirmarPago()` para descontar cupo:

```java
@Transactional
@Override
public InscripcionProveedorDTO confirmarPago(Long idInscripcion, Long idPagoEvento) {
    // 1. Buscar la inscripción
    InscripcionProveedor inscripcion = inscripcionRepository.findById(idInscripcion)
        .orElseThrow(() -> new CustomException("Inscripción no encontrada."));
    
    // 2. Validar el estado actual
    if (inscripcion.getEstadoCupo() == EstadoCupo.CONFIRMADO) {
        throw new CustomException("Esta inscripción ya ha sido confirmada.");
    }
    
    // 3. Buscar el pago
    PagoEvento pago = pagoEventoRepository.findById(idPagoEvento)
        .orElseThrow(() -> new CustomException("Pago no encontrado."));
    
    if (pago.getEstado() != EstadoPago.EXITOSO) {
        throw new CustomException("El pago debe estar en estado EXITOSO para confirmar la inscripción.");
    }
    
    // 4. ✅ DESCONTAR CUPO SOLO AQUÍ
    Evento evento = inscripcion.getEvento();
    
    // Verificar que aún haya cupos (por si varios proveedores pagan al mismo tiempo)
    if (evento.getCuposOcupados() >= evento.getCuposMaximosProveedor()) {
        throw new CustomException("Lo sentimos, los cupos se agotaron mientras procesábamos tu pago.");
    }
    
    evento.setCuposOcupados(evento.getCuposOcupados() + 1);
    eventoRepository.save(evento);
    
    // 5. Confirmar inscripción
    inscripcion.confirmarInscripcion();
    inscripcion.setPagoEvento(pago);
    
    InscripcionProveedor actualizada = inscripcionRepository.save(inscripcion);
    
    return mapearConDatosVisualizacion(actualizada);
}
```

---

#### **8. Si salgo del checkout, poder volver con botón "Completar Pago"**

**Problema:**  
Al salir del checkout, la inscripción queda PENDIENTE pero no aparece en `mis-inscripciones` ni hay forma de volver al pago.

**Solución:**

**Archivo:** `src/main/java/com/example/campolibre/Implement/InscripcionProveedorImplement.java`

Modificar método para incluir inscripciones PENDIENTES:

```java
@Override
public List<InscripcionProveedorDTO> obtenerTodasLasInscripcionesDeProveedor(Long idProveedor) {
    // ✅ Traer TODAS: CONFIRMADAS, PENDIENTES y CANCELADAS
    List<InscripcionProveedor> inscripciones = inscripcionRepository.findByProveedorIdUsuario(idProveedor);
    
    return inscripciones.stream()
        .map(this::mapearConDatosVisualizacion)
        .collect(Collectors.toList());
}
```

**Archivo:** `src/main/java/com/example/campolibre/Controller/EventoController.java`

Modificar endpoint `mis-inscripciones`:

```java
@GetMapping("/mis-inscripciones")
public String misInscripciones(Model model, Authentication authentication) {
    if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("PROVEEDOR"))) {
        return "redirect:/eventos";
    }
    
    String email = authentication.getName();
    UsuarioDTO usuario = usuarioService.obtenerUsuarioPorEmail(email);
    
    // ✅ Obtener TODAS las inscripciones (incluye PENDIENTES)
    var inscripciones = inscripcionProveedorService.obtenerTodasLasInscripcionesDeProveedor(usuario.getId_usuario());
    
    model.addAttribute("inscripciones", inscripciones);
    model.addAttribute("tipoLista", "mis_inscripciones");
    return "evento/mis-inscripciones";
}
```

**Archivo:** `src/main/resources/templates/evento/mis-inscripciones.html`

Modificar para mostrar botón "Completar Pago" para inscripciones PENDIENTES:

```html
<!-- Modificar la sección de acciones en la tarjeta de inscripción -->
<div class="card-footer bg-white d-flex justify-content-between align-items-center">
    <div>
        <a th:href="@{/eventos/ver/{id}(id=${inscripcion.id_evento})}"
           class="btn btn-outline-primary btn-sm">
            <i class="bi bi-eye"></i> Ver Detalles
        </a>
        
        <!-- ✅ Botón COMPLETAR PAGO para inscripciones PENDIENTES -->
        <a th:if="${inscripcion.estadoCupo == T(com.example.campolibre.Enum.EstadoCupo).PENDIENTE}"
           th:href="@{/pagos-eventos/checkout/{id}(id=${inscripcion.id_inscripcion})}"
           class="btn btn-warning btn-sm">
            <i class="bi bi-credit-card"></i> Completar Pago
        </a>
        
        <!-- Botón Ver QR para CONFIRMADAS -->
        <a th:if="${inscripcion.estadoCupo == T(com.example.campolibre.Enum.EstadoCupo).CONFIRMADO}"
           th:href="@{/pagos-eventos/codigo-confirmacion/{id}(id=${inscripcion.id_inscripcion})}"
           class="btn btn-success btn-sm">
            <i class="bi bi-qr-code"></i> Ver QR
        </a>
    </div>
    
    <div>
        <!-- Botón Cancelar solo para CONFIRMADAS -->
        <button th:if="${inscripcion.estadoCupo == T(com.example.campolibre.Enum.EstadoCupo).CONFIRMADO}"
                class="btn btn-link btn-sm text-danger text-decoration-none p-0"
                onclick="confirmarCancelacion(this)"
                th:data-inscripcion-id="${inscripcion.id_inscripcion}"
                th:data-evento-nombre="${inscripcion.nombreEvento}">
            <i class="bi bi-x-circle"></i> Cancelar
        </button>
    </div>
</div>
```

**Archivo:** `src/main/resources/templates/evento/view.html`

Agregar botón "Completar Pago" cuando hay pago pendiente:

```html
<!-- Botón Completar Pago - Si tiene pago pendiente -->
<a th:if="${isPagoPendiente}"
   th:href="@{/pagos-eventos/checkout/{id}(id=${idInscripcionPendiente})}"
   class="btn btn-warning btn-lg w-100">
    <i class="bi bi-credit-card"></i> Completar Pago
</a>

<!-- Mensaje informativo -->
<div th:if="${isPagoPendiente}" class="alert alert-info mt-3">
    <i class="bi bi-info-circle"></i>
    Tienes una inscripción pendiente de pago para este evento. Completa el pago para asegurar tu cupo.
</div>
```

---

### **GRUPO 6: QR Y REPORTES**

#### **9. Arreglar visualización de QR en impresión**

**Problema:**  
El QR no se visualiza al imprimir la página, pero sí funciona la descarga.

**Solución:**  
Eliminar el botón "Imprimir" que no funciona y dejar solo el de descarga.

**Archivo:** `src/main/resources/templates/pago-evento/codigo-confirmacion.html`

Eliminar botón de impresión y dejar solo descarga:

```html
<!-- Buscar y ELIMINAR el botón de imprimir -->
<!-- ANTES:
<button class="btn btn-outline-dark" onclick="window.print()">
    <i class="bi bi-printer"></i> Imprimir
</button>
-->

<!-- MANTENER solo el botón de descarga -->
<button class="btn btn-success" onclick="descargarQR()">
    <i class="bi bi-download"></i> Descargar QR
</button>

<!-- Agregar función de descarga si no existe -->
<script>
function descargarQR() {
    const canvas = document.querySelector('#qrcode canvas');
    if (canvas) {
        const url = canvas.toDataURL('image/png');
        const link = document.createElement('a');
        link.download = `QR-${codigoConfirmacion}.png`;
        link.href = url;
        link.click();
    } else {
        alert('Error: No se pudo generar el QR. Recarga la página e intenta nuevamente.');
    }
}
</script>
```

---

#### **10. Imprimir Lista de Proveedores Inscritos**

**Problema:**  
No se visualiza correctamente la lista de proveedores inscritos al intentar imprimir.

**Solución:**

**Archivo:** `src/main/java/com/example/campolibre/Controller/EventoController.java`

Verificar que existe el endpoint (ya debería estar):

```java
@GetMapping("/admin/reporte-inscripciones/{idEvento}")
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public String verReporteInscripciones(@PathVariable Long idEvento,
                                      Model model,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
    try {
        // Obtener evento
        EventoDTO evento = eventoService.obtenerEventoPorId(idEvento);
        
        // Obtener inscripciones
        List<InscripcionProveedorDTO> inscripciones = 
            inscripcionProveedorService.obtenerInscripcionesPorEvento(idEvento);
        
        // Filtrar solo CONFIRMADAS
        List<InscripcionProveedorDTO> confirmadas = inscripciones.stream()
            .filter(i -> i.getEstadoCupo() == EstadoCupo.CONFIRMADO)
            .collect(Collectors.toList());
        
        model.addAttribute("evento", evento);
        model.addAttribute("inscripciones", confirmadas);
        model.addAttribute("totalInscritos", confirmadas.size());
        
        return "evento/reporte-inscripciones";
        
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Error al cargar reporte: " + e.getMessage());
        return "redirect:/eventos/admin/todos";
    }
}
```

**Archivo:** `src/main/resources/templates/evento/reporte-inscripciones.html` (CREAR NUEVO)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security"
      th:replace="~{layout/base :: layout(~{::title}, ~{::content})}">
<head>
    <title>Proveedores Inscritos - Campo Libre</title>
    <style>
        @media print {
            .no-print { display: none !important; }
            .table { font-size: 12px; }
        }
        
        .search-box {
            position: relative;
            margin-bottom: 1rem;
        }
        .search-box input {
            padding-left: 2.5rem;
        }
        .search-box i {
            position: absolute;
            left: 1rem;
            top: 50%;
            transform: translateY(-50%);
            color: #6c757d;
        }
    </style>
</head>
<body>

<main th:fragment="content">
    <div class="container mt-4 mb-5">
        
        <!-- Header -->
        <div class="d-flex justify-content-between align-items-center mb-4 no-print">
            <div>
                <h2>
                    <i class="bi bi-people-fill text-primary"></i>
                    Proveedores Inscritos
                </h2>
                <p class="text-muted mb-0" th:text="${evento.nombre}">Nombre del Evento</p>
            </div>
            <div>
                <button onclick="window.print()" class="btn btn-outline-primary">
                    <i class="bi bi-printer"></i> Imprimir
                </button>
                <a th:href="@{/eventos/admin/todos}" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left"></i> Volver
                </a>
            </div>
        </div>
        
        <!-- Información del Evento -->
        <div class="row mb-4">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-body">
                        <h6 class="text-muted mb-3">Información del Evento</h6>
                        <p class="mb-2">
                            <strong>Fecha:</strong> 
                            <span th:text="${#temporals.format(evento.fecha_evento, 'dd/MM/yyyy')}"></span>
                        </p>
                        <p class="mb-2">
                            <strong>Hora:</strong> 
                            <span th:text="${#temporals.format(evento.hora_evento, 'HH:mm')}"></span>
                        </p>
                        <p class="mb-0">
                            <strong>Ubicación:</strong> 
                            <span th:text="${evento.ubicacion}"></span>
                        </p>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card">
                    <div class="card-body">
                        <h6 class="text-muted mb-3">Estadísticas</h6>
                        <p class="mb-2">
                            <strong>Total Inscritos:</strong> 
                            <span class="badge bg-success" th:text="${totalInscritos}">0</span>
                        </p>
                        <p class="mb-2">
                            <strong>Cupos Totales:</strong> 
                            <span th:text="${evento.cuposMaximosProveedor}">0</span>
                        </p>
                        <p class="mb-0">
                            <strong>Cupos Disponibles:</strong> 
                            <span class="badge bg-info" th:text="${evento.cuposDisponibles}">0</span>
                        </p>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- Buscador -->
        <div class="search-box no-print">
            <i class="bi bi-search"></i>
            <input type="text" 
                   id="searchInput" 
                   class="form-control" 
                   placeholder="Buscar por nombre, email o código..."
                   onkeyup="filtrarTabla()">
        </div>
        
        <!-- Tabla de Inscritos -->
        <div class="card">
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0" id="tablaInscritos">
                        <thead class="table-light">
                            <tr>
                                <th>#</th>
                                <th>Proveedor</th>
                                <th>Email</th>
                                <th>Teléfono</th>
                                <th>Código Confirmación</th>
                                <th>Fecha Inscripción</th>
                                <th class="no-print">Estado</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr th:each="inscripcion, iterStat : ${inscripciones}">
                                <td th:text="${iterStat.count}">1</td>
                                <td>
                                    <strong th:text="${inscripcion.nombreProveedor}">Nombre</strong>
                                </td>
                                <td th:text="${inscripcion.emailProveedor}">email@example.com</td>
                                <td th:text="${inscripcion.telefonoProveedor}">123456789</td>
                                <td>
                                    <code class="text-primary" 
                                          th:text="${inscripcion.codigoConfirmacion}">EVT-XXX</code>
                                </td>
                                <td th:text="${#temporals.format(inscripcion.fechaInscripcion, 'dd/MM/yyyy HH:mm')}">
                                    01/01/2024
                                </td>
                                <td class="no-print">
                                    <span class="badge bg-success">
                                        <i class="bi bi-check-circle"></i> Confirmado
                                    </span>
                                </td>
                            </tr>
                            
                            <!-- Mensaje si no hay inscritos -->
                            <tr th:if="${#lists.isEmpty(inscripciones)}">
                                <td colspan="7" class="text-center py-5 text-muted">
                                    <i class="bi bi-inbox fs-1"></i>
                                    <p class="mb-0 mt-2">No hay proveedores inscritos aún</p>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        
        <!-- Pie de página para impresión -->
        <div class="mt-4 text-center text-muted" style="display: none;">
            <small>
                Generado el <span th:text="${#temporals.format(#temporals.createNow(), 'dd/MM/yyyy HH:mm')}"></span>
                | Campo Libre - Sistema de Gestión de Eventos Agrícolas
            </small>
        </div>
        
    </div>
</main>

<script>
function filtrarTabla() {
    const input = document.getElementById('searchInput');
    const filter = input.value.toUpperCase();
    const table = document.getElementById('tablaInscritos');
    const tr = table.getElementsByTagName('tr');
    
    for (let i = 1; i < tr.length; i++) {
        const td = tr[i].getElementsByTagName('td');
        let encontrado = false;
        
        // Buscar en nombre, email y código
        for (let j = 1; j <= 4; j++) {
            if (td[j]) {
                const txtValue = td[j].textContent || td[j].innerText;
                if (txtValue.toUpperCase().indexOf(filter) > -1) {
                    encontrado = true;
                    break;
                }
            }
        }
        
        tr[i].style.display = encontrado ? '' : 'none';
    }
}

// Mostrar fecha de generación al imprimir
window.addEventListener('beforeprint', function() {
    document.querySelector('.mt-4.text-center').style.display = 'block';
});

window.addEventListener('afterprint', function() {
    document.querySelector('.mt-4.text-center').style.display = 'none';
});
</script>

</body>
</html>
```

---

#### **11. Filtro de Búsqueda en Lista de Proveedores**

**Solución:**  
Ya implementado en el código anterior de `reporte-inscripciones.html` con la función `filtrarTabla()`.

Permite buscar por:
- Nombre del proveedor
- Email
- Teléfono
- Código de confirmación

---

### **GRUPO 7: GESTIÓN DE ESTADOS DE EVENTOS**

#### **12. Eventos Cancelados - No mostrar botón Editar ni Eliminar**

**Problema:**  
Los eventos cancelados aún muestran botones de edición y eliminación.

**Solución:**

**Archivo:** `src/main/resources/templates/evento/admin-list.html`

Modificar la sección de botones de acción:

```html
<!-- Dentro de la columna de acciones -->
<td class="text-end pe-4">
    <div class="btn-group">
        
        <!-- Ver Proveedores Inscritos -->
        <a th:href="@{/eventos/admin/reporte-inscripciones/{id}(id=${evento.id_evento})}"
           class="btn btn-sm btn-outline-info"
           title="Ver Proveedores Inscritos"
           th:if="${evento.cuposOcupados > 0}">
            <i class="bi bi-people-fill"></i>
        </a>
        
        <!-- ✅ Botón Editar - OCULTO para eventos CANCELADOS y FINALIZADOS -->
        <a th:if="${evento.estado.name() != 'CANCELADO' and evento.estado.name() != 'FINALIZADO'}"
           th:href="@{/eventos/editar/{id}(id=${evento.id_evento})}"
           class="btn btn-sm btn-outline-primary" 
           title="Editar">
            <i class="bi bi-pencil"></i>
        </a>
        
        <!-- Publicar (solo BORRADOR) -->
        <a th:if="${evento.estado.name() == 'BORRADOR'}"
           th:href="@{/eventos/aprobar/{id}(id=${evento.id_evento})}"
           class="btn btn-sm btn-success" 
           title="Publicar"
           onclick="return confirm('¿Publicar este evento? Se notificará a todos los proveedores.')">
            <i class="bi bi-check-circle"></i>
        </a>
        
        <!-- ✅ Iniciar Evento (PUBLICADO → EN_CURSO) -->
        <a th:if="${evento.estado.name() == 'PUBLICADO'}"
           th:href="@{/eventos/iniciar/{id}(id=${evento.id_evento})}"
           class="btn btn-sm btn-info" 
           title="Iniciar Evento (Cambiar a En Curso)"
           onclick="return confirm('¿Marcar este evento como En Curso?')">
            <i class="bi bi-play-circle"></i>
        </a>
        
        <!-- ✅ Finalizar Evento (EN_CURSO → FINALIZADO) -->
        <a th:if="${evento.estado.name() == 'EN_CURSO'}"
           th:href="@{/eventos/finalizar/{id}(id=${evento.id_evento})}"
           class="btn btn-sm btn-secondary" 
           title="Finalizar Evento"
           onclick="return confirm('¿Marcar este evento como Finalizado? Esta acción no se puede revertir.')">
            <i class="bi bi-flag-fill"></i>
        </a>
        
        <!-- ✅ Cancelar - OCULTO para eventos CANCELADOS y FINALIZADOS -->
        <a th:if="${evento.estado.name() != 'CANCELADO' and evento.estado.name() != 'FINALIZADO'}"
           th:href="@{/eventos/eliminar/{id}(id=${evento.id_evento})}"
           class="btn btn-sm btn-danger" 
           title="Cancelar evento"
           onclick="return confirm('¿Cancelar este evento? Esta acción no se puede revertir.')">
            <i class="bi bi-x-circle"></i>
        </a>
    </div>
</td>
```

**Archivo:** `src/main/resources/templates/evento/pendientes.html`

Aplicar misma lógica:

```html
<!-- Modificar sección de botones de acción -->
<div class="btn-group btn-group-sm" role="group">
    <!-- Ver -->
    <a th:href="@{/eventos/ver/{id}(id=${evento.id_evento})}"
       class="btn btn-outline-info"
       title="Ver detalles">
        <i class="bi bi-eye"></i>
    </a>
    
    <!-- ✅ Editar - Solo si NO está cancelado -->
    <a th:if="${evento.estado != T(com.example.campolibre.Enum.EstadoEvento).CANCELADO}"
       th:href="@{/eventos/editar/{id}(id=${evento.id_evento})}"
       class="btn btn-outline-primary"
       title="Editar">
        <i class="bi bi-pencil"></i>
    </a>
    
    <!-- Publicar (solo BORRADOR) -->
    <a th:if="${evento.estado == T(com.example.campolibre.Enum.EstadoEvento).BORRADOR}"
       th:href="@{/eventos/aprobar/{id}(id=${evento.id_evento})}"
       class="btn btn-outline-success"
       title="Publicar evento"
       onclick="return confirm('¿Publicar este evento?')">
        <i class="bi bi-check-circle"></i>
    </a>
    
    <!-- ✅ Cancelar - Solo si NO está cancelado -->
    <a th:if="${evento.estado != T(com.example.campolibre.Enum.EstadoEvento).CANCELADO}"
       th:href="@{/eventos/eliminar/{id}(id=${evento.id_evento})}"
       class="btn btn-outline-danger"
       title="Cancelar evento"
       onclick="return confirm('¿Cancelar este evento?')">
        <i class="bi bi-x-circle"></i>
    </a>
</div>
```

---

#### **13. Validar cambio automático/manual de PUBLICADO a EN_CURSO**

**Solución:**  
Implementación **manual con botón** para el administrador.

**Archivo:** `src/main/java/com/example/campolibre/Controller/EventoController.java`

Agregar endpoints para cambiar estados:

```java
/**
 * ✅ NUEVO: Iniciar evento (PUBLICADO → EN_CURSO)
 */
@GetMapping("/iniciar/{id}")
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public String iniciarEvento(@PathVariable Long id,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
    try {
        eventoService.cambiarEstadoEvento(id, EstadoEvento.EN_CURSO);
        redirectAttributes.addFlashAttribute("success", "Evento marcado como En Curso");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
    }
    return "redirect:/eventos/admin/todos";
}

/**
 * ✅ NUEVO: Finalizar evento (EN_CURSO → FINALIZADO)
 */
@GetMapping("/finalizar/{id}")
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
public String finalizarEvento(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
    try {
        eventoService.cambiarEstadoEvento(id, EstadoEvento.FINALIZADO);
        redirectAttributes.addFlashAttribute("success", "Evento finalizado correctamente");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
    }
    return "redirect:/eventos/admin/todos";
}
```

**Archivo:** `src/main/java/com/example/campolibre/Implement/EventoImplement.java`

Agregar validaciones en `cambiarEstadoEvento()`:

```java
@Override
public void cambiarEstadoEvento(Long id, EstadoEvento nuevoEstado) {
    Evento evento = eventoRepository.findById(id)
        .orElseThrow(() -> new CustomException("Evento no encontrado"));
    
    EstadoEvento estadoActual = evento.getEstado();
    
    // ✅ VALIDACIONES DE TRANSICIONES DE ESTADO
    switch (nuevoEstado) {
        case PUBLICADO:
            if (estadoActual != EstadoEvento.BORRADOR) {
                throw new CustomException("Solo se pueden publicar eventos en borrador");
            }
            // Validaciones existentes...
            if (evento.getFechaPublicacion() == null) {
                evento.setFechaPublicacion(LocalDateTime.now());
            }
            break;
            
        case EN_CURSO:
            if (estadoActual != EstadoEvento.PUBLICADO) {
                throw new CustomException("Solo se pueden iniciar eventos publicados");
            }
            break;
            
        case FINALIZADO:
            if (estadoActual != EstadoEvento.EN_CURSO) {
                throw new CustomException("Solo se pueden finalizar eventos en curso");
            }
            break;
            
        case CANCELADO:
            if (estadoActual == EstadoEvento.FINALIZADO) {
                throw new CustomException("No se pueden cancelar eventos finalizados");
            }
            break;
    }
    
    evento.setEstado(nuevoEstado);
    eventoRepository.save(evento);
    
    // Notificaciones según estado
    if (nuevoEstado == EstadoEvento.PUBLICADO) {
        notificarProveedoresEventoPublicado(evento);
    }
}
```

---

#### **14. No asignar Patrocinador con estado Archivado (inactivo)**

**Problema:**  
Se pueden asignar patrocinadores inactivos a eventos.

**Solución:**

**Archivo:** `src/main/java/com/example/campolibre/Implement/EventoImplement.java`

Agregar validación en `crearEvento()` y `actualizarEvento()`:

```java
@Override
@Transactional
public EventoDTO crearEvento(EventoCreacionDTO eventoCreacionDTO, Long idAdmin, MultipartFile imagen) {
    // ... código existente ...
    
    // ✅ Validar que el patrocinador esté ACTIVO
    Patrocinador patrocinador = patrocinadorRepository.findById(eventoCreacionDTO.getId_patrocinador())
        .orElseThrow(() -> new CustomException("Patrocinador no encontrado"));
    
    if (!patrocinador.getActivo()) {
        throw new CustomException("No se puede asignar un patrocinador archivado/inactivo a un evento");
    }
    
    // ... resto del código ...
}

@Override
@Transactional
public EventoDTO actualizarEvento(Long id, EventoCreacionDTO eventoCreacionDTO, MultipartFile imagen) {
    // ... código existente ...
    
    // ✅ Si se cambia el patrocinador, validar que esté activo
    if (eventoCreacionDTO.getId_patrocinador() != null) {
        Patrocinador patrocinador = patrocinadorRepository.findById(eventoCreacionDTO.getId_patrocinador())
            .orElseThrow(() -> new CustomException("Patrocinador no encontrado"));
        
        if (!patrocinador.getActivo()) {
            throw new CustomException("No se puede asignar un patrocinador archivado/inactivo a un evento");
        }
        
        eventoExistente.setPatrocinador(patrocinador);
    }
    
    // ... resto del código ...
}
```

**Archivo:** `src/main/resources/templates/evento/form.html` y `evento/edit.html`

Filtrar solo patrocinadores activos en el dropdown:

```html
<!-- En el select de patrocinador -->
<select class="form-select"
        id="id_patrocinador"
        th:field="*{id_patrocinador}"
        required>
    <option value="" selected>-- Seleccione --</option>
    <!-- ✅ Filtrar solo patrocinadores ACTIVOS -->
    <option th:each="patrocinador : ${patrocinadores}"
            th:if="${patrocinador.activo}"
            th:value="${patrocinador.id_patrocinador}"
            th:text="${patrocinador.nombre}">
    </option>
</select>
```

**Archivo:** `src/main/java/com/example/campolibre/Controller/EventoController.java`

Filtrar patrocinadores activos al cargar formularios:

```java
@GetMapping("/crear")
public String mostrarFormularioCreacion(Model model, Authentication authentication) {
    // ... código existente ...
    
    // ✅ Cargar solo patrocinadores ACTIVOS
    List<PatrocinadorDTO> patrocinadores = patrocinadorService.obtenerTodosLosPatrocinadores()
        .stream()
        .filter(PatrocinadorDTO::getActivo)
        .collect(Collectors.toList());
    
    model.addAttribute("patrocinadores", patrocinadores);
    // ... resto del código ...
}

@GetMapping("/editar/{id}")
public String mostrarFormularioEdicion(@PathVariable Long id, Model model, Authentication authentication) {
    // ... código existente ...
    
    // ✅ Cargar solo patrocinadores ACTIVOS
    List<PatrocinadorDTO> patrocinadores = patrocinadorService.obtenerTodosLosPatrocinadores()
        .stream()
        .filter(PatrocinadorDTO::getActivo)
        .collect(Collectors.toList());
    
    model.addAttribute("patrocinadores", patrocinadores);
    // ... resto del código ...
}
```

---

## **RESUMEN DE ARCHIVOS A MODIFICAR**

### **Backend - Java:**

1. **InscripcionProveedorImplement.java**
    - Validar estado evento en `solicitarCupo()`
    - Implementar `cancelarInscripcion()` con cálculo de reembolso
    - Modificar `confirmarPago()` para descontar cupo solo al pagar
    - Modificar `obtenerTodasLasInscripcionesDeProveedor()` para incluir PENDIENTES

2. **EventoController.java**
    - Modificar `mostrarFormularioEdicion()` para cargar fecha/hora
    - Agregar endpoint `iniciarEvento()`
    - Agregar endpoint `finalizarEvento()`
    - Modificar `cancelarInscripcion()` con mensaje de reembolso
    - Filtrar patrocinadores activos en formularios
    - Agregar endpoint `verReporteInscripciones()`

3. **EventoImplement.java**
    - Validar transiciones de estado en `cambiarEstadoEvento()`
    - Validar patrocinador activo en `crearEvento()` y `actualizarEvento()`

4. **PagoEventoController.java**
    - Modificar `procesarPago()` para recibir datos de PSE/entidad bancaria

5. **Enums (CREAR NUEVOS):**
    - `EntidadBancaria.java`
    - `TipoPersonaPSE.java`
    - Modificar `EstadoPago.java` (agregar REEMBOLSADO)

6. **Entities:**
    - Modificar `PagoEvento.java` (agregar campos PSE)

7. **DTOs:**
    - Modificar `PagoEventoCreacionDTO.java` (agregar campos PSE)

### **Frontend - HTML:**

8. **evento/view.html**
    - Deshabilitar inscripción si evento no está PUBLICADO
    - Agregar botón "Completar Pago" si hay pago pendiente
    - Agregar botón "Volver"

9. **evento/edit.html**
    - Verificar binding correcto de fecha y hora
    - Filtrar patrocinadores activos

10. **evento/form.html**
    - Filtrar patrocinadores activos

11. **evento/mis-inscripciones.html**
    - Mostrar inscripciones PENDIENTES
    - Botón "Completar Pago" para pendientes
    - Mejorar script de cancelación

12. **evento/admin-list.html**
    - Ocultar editar/cancelar para eventos CANCELADOS/FINALIZADOS
    - Agregar botón "Iniciar Evento"
    - Agregar botón "Finalizar Evento"

13. **evento/pendientes.html**
    - Ocultar editar/cancelar para eventos CANCELADOS

14. **pago-evento/checkout.html**
    - Rediseñar formulario con campos PSE
    - Agregar selección de entidad bancaria
    - Bloquear botón pagar hasta aceptar términos

15. **pago-evento/codigo-confirmacion.html**
    - Eliminar botón "Imprimir"
    - Mantener solo botón "Descargar QR"

16. **evento/reporte-inscripciones.html** (CREAR NUEVO)
    - Vista para imprimir lista de proveedores
    - Filtro de búsqueda
    - Estilos de impresión

---

## **ORDEN DE IMPLEMENTACIÓN SUGERIDO**

### **Fase 1: Correcciones Básicas**
1. Botón Volver en evento/view
2. Arreglar carga de fecha/hora en formulario edición
3. Validaciones de inscripción por estado de evento

### **Fase 2: Sistema de Pagos**
4. Crear enums de PSE y entidades bancarias
5. Modificar entidad PagoEvento
6. Rediseñar formulario de checkout
7. Bloquear botón pagar sin términos

### **Fase 3: Gestión de Cupos**
8. No descontar cupo hasta pagar
9. Mostrar inscripciones pendientes
10. Botón "Completar Pago"

### **Fase 4: Cancelaciones y Reembolsos**
11. Lógica de cálculo de reembolso
12. Modificar cancelación de inscripción
13. Mensajes informativos

### **Fase 5: Reportes y Visualización**
14. Crear vista de reporte de inscritos
15. Implementar filtro de búsqueda
16. Arreglar QR (eliminar botón imprimir)

### **Fase 6: Estados de Eventos**
17. Botones iniciar/finalizar evento
18. Ocultar editar/cancelar en eventos finalizados
19. Validar patrocinador activo
20. Validar transiciones de estado

---

## **NOTAS IMPORTANTES**

- ⚠️ **Migraciones de BD:** Los nuevos campos en `PagoEvento` requieren migración
- ⚠️ **Transacciones:** Usar `@Transactional` en operaciones críticas
- ⚠️ **Validaciones:** Siempre validar estado antes de cambios
- ⚠️ **Reembolsos:** Son simulados, no hay integración con pasarela real
- ⚠️ **Notificaciones:** Incluir mensajes claros sobre procesos de reembolso

---

## **VERIFICACIÓN POST-IMPLEMENTACIÓN**

✅ No se puede inscribir a eventos cancelados/finalizados/en curso  
✅ Botón "Volver" funciona en vista de evento  
✅ Formulario de edición carga fecha y hora correctamente  
✅ Cancelación muestra mensaje de reembolso  
✅ Formulario PSE con entidades bancarias funciona  
✅ Botón pagar bloqueado sin aceptar términos  
✅ Cupos no bajan hasta confirmar pago  
✅ Inscripciones pendientes aparecen en mis-inscripciones  
✅ Botón "Completar Pago" funciona  
✅ QR se descarga correctamente (sin botón imprimir)  
✅ Lista de proveedores se imprime correctamente  
✅ Filtro de búsqueda funciona  
✅ Eventos cancelados no muestran editar/eliminar  
✅ Botones iniciar/finalizar evento funcionan  
✅ No se pueden asignar patrocinadores inactivos

---

**Fin del documento de instrucciones**