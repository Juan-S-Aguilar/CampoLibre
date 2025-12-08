/**
 * ============================================================
 * VALIDACIONES PARA AUTENTICACIÓN - CAMPO LIBRE
 * ============================================================
 * Archivo: validaciones-auth.js
 * Uso: Login (login.html) y Registro (register.html)
 * ============================================================
 */

// ========== CONFIGURACIÓN GENERAL ==========
const CONFIG = {
    PASSWORD_MIN_LENGTH: 6,
    TELEFONO_LENGTH: 10,
    NOMBRE_MIN_LENGTH: 3,
    NOMBRE_MAX_LENGTH: 100,
    EMAIL_MAX_LENGTH: 100,
    DOCUMENTO_MIN_LENGTH: 5,
    DOCUMENTO_MAX_LENGTH: 20
};

// ========== EXPRESIONES REGULARES ==========
const REGEX = {
    // Email robusto
    email: /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,

    // Solo números (para teléfono y documentos)
    soloNumeros: /^[0-9]+$/,

    // Solo letras, espacios, tildes y ñ (para nombres)
    soloLetras: /^[a-záéíóúñA-ZÁÉÍÓÚÑ\s]+$/,

    // Alfanumérico (para documentos que pueden tener letras)
    alfanumerico: /^[a-zA-Z0-9]+$/
};

// ========== UTILIDADES DE FEEDBACK VISUAL ==========
/**
 * Mostrar error en un campo
 */
function mostrarError(campo, mensaje) {
    const formGroup = campo.closest('.mb-3') || campo.closest('.form-group');

    // Añadir clase de error al campo
    campo.classList.add('is-invalid');
    campo.classList.remove('is-valid');

    // Buscar o crear el div de feedback
    let feedback = formGroup.querySelector('.invalid-feedback');
    if (!feedback) {
        feedback = document.createElement('div');
        feedback.className = 'invalid-feedback';
        campo.parentNode.insertBefore(feedback, campo.nextSibling);
    }

    feedback.textContent = mensaje;
    feedback.style.display = 'block';
}

/**
 * Mostrar campo como válido
 */
function mostrarValido(campo) {
    const formGroup = campo.closest('.mb-3') || campo.closest('.form-group');

    campo.classList.remove('is-invalid');
    campo.classList.add('is-valid');

    // Ocultar mensaje de error
    const feedback = formGroup.querySelector('.invalid-feedback');
    if (feedback) {
        feedback.style.display = 'none';
    }
}

/**
 * Limpiar validación de un campo
 */
function limpiarValidacion(campo) {
    campo.classList.remove('is-invalid', 'is-valid');

    const formGroup = campo.closest('.mb-3') || campo.closest('.form-group');
    const feedback = formGroup.querySelector('.invalid-feedback');
    if (feedback) {
        feedback.style.display = 'none';
    }
}

// ========== VALIDACIONES INDIVIDUALES ==========

/**
 * Validar email
 */
function validarEmail(campo) {
    const email = campo.value.trim();

    if (email === '') {
        mostrarError(campo, 'El correo electrónico es obligatorio');
        return false;
    }

    if (email.length > CONFIG.EMAIL_MAX_LENGTH) {
        mostrarError(campo, `El correo no puede superar ${CONFIG.EMAIL_MAX_LENGTH} caracteres`);
        return false;
    }

    if (!REGEX.email.test(email)) {
        mostrarError(campo, 'Ingresa un correo electrónico válido (ejemplo: usuario@dominio.com)');
        return false;
    }

    mostrarValido(campo);
    return true;
}

/**
 * Validar contraseña
 */
function validarContrasena(campo, esObligatorio = true) {
    const contrasena = campo.value;

    // Si el campo está vacío y NO es obligatorio (ej: en edición)
    if (contrasena === '' && !esObligatorio) {
        limpiarValidacion(campo);
        return true;
    }

    if (contrasena === '' && esObligatorio) {
        mostrarError(campo, 'La contraseña es obligatoria');
        return false;
    }

    if (contrasena.length < CONFIG.PASSWORD_MIN_LENGTH) {
        mostrarError(campo, `La contraseña debe tener mínimo ${CONFIG.PASSWORD_MIN_LENGTH} caracteres`);
        return false;
    }

    // Validación de fortaleza (opcional - puedes activarla si quieres)
    // if (!/[A-Z]/.test(contrasena)) {
    //     mostrarError(campo, 'La contraseña debe tener al menos una mayúscula');
    //     return false;
    // }

    mostrarValido(campo);
    return true;
}

/**
 * Validar confirmación de contraseña
 */
function validarConfirmacionContrasena(campoContrasena, campoConfirmacion) {
    const contrasena = campoContrasena.value;
    const confirmacion = campoConfirmacion.value;

    if (confirmacion === '') {
        mostrarError(campoConfirmacion, 'Debes confirmar tu contraseña');
        return false;
    }

    if (contrasena !== confirmacion) {
        mostrarError(campoConfirmacion, 'Las contraseñas no coinciden');
        return false;
    }

    mostrarValido(campoConfirmacion);
    return true;
}

/**
 * Validar nombre completo
 */
function validarNombre(campo) {
    const nombre = campo.value.trim();

    if (nombre === '') {
        mostrarError(campo, 'El nombre completo es obligatorio');
        return false;
    }

    if (nombre.length < CONFIG.NOMBRE_MIN_LENGTH) {
        mostrarError(campo, `El nombre debe tener mínimo ${CONFIG.NOMBRE_MIN_LENGTH} caracteres`);
        return false;
    }

    if (nombre.length > CONFIG.NOMBRE_MAX_LENGTH) {
        mostrarError(campo, `El nombre no puede superar ${CONFIG.NOMBRE_MAX_LENGTH} caracteres`);
        return false;
    }

    if (!REGEX.soloLetras.test(nombre)) {
        mostrarError(campo, 'El nombre solo puede contener letras y espacios');
        return false;
    }

    // Validar que no sean solo espacios
    if (nombre.replace(/\s/g, '').length === 0) {
        mostrarError(campo, 'El nombre no puede contener solo espacios');
        return false;
    }

    mostrarValido(campo);
    return true;
}

/**
 * Validar teléfono colombiano
 */
function validarTelefono(campo, esObligatorio = true) {
    const telefono = campo.value.trim();

    // Si el campo está vacío y NO es obligatorio
    if (telefono === '' && !esObligatorio) {
        limpiarValidacion(campo);
        return true;
    }

    if (telefono === '' && esObligatorio) {
        mostrarError(campo, 'El teléfono es obligatorio');
        return false;
    }

    if (!REGEX.soloNumeros.test(telefono)) {
        mostrarError(campo, 'El teléfono solo puede contener números');
        return false;
    }

    if (telefono.length !== CONFIG.TELEFONO_LENGTH) {
        mostrarError(campo, `El teléfono debe tener exactamente ${CONFIG.TELEFONO_LENGTH} dígitos`);
        return false;
    }

    // Validar que empiece con 3 (teléfonos móviles en Colombia)
    if (!telefono.startsWith('3')) {
        mostrarError(campo, 'El teléfono móvil debe iniciar con 3 (Ej: 3001234567)');
        return false;
    }

    mostrarValido(campo);
    return true;
}

/**
 * Validar tipo de documento
 */
function validarTipoDocumento(campo) {
    const tipoDocumento = campo.value;

    if (tipoDocumento === '' || tipoDocumento === null) {
        mostrarError(campo, 'Debes seleccionar un tipo de documento');
        return false;
    }

    mostrarValido(campo);
    return true;
}

/**
 * Validar número de documento según el tipo
 */
function validarDocumento(campoDocumento, campoTipoDocumento) {
    const documento = campoDocumento.value.trim();
    const tipoDocumento = campoTipoDocumento.value;

    if (documento === '') {
        mostrarError(campoDocumento, 'El número de documento es obligatorio');
        return false;
    }

    // Validación según tipo de documento
    switch(tipoDocumento) {
        case 'CC': // Cédula de Ciudadanía
        case 'CE': // Cédula de Extranjería
            if (!REGEX.soloNumeros.test(documento)) {
                mostrarError(campoDocumento, 'La cédula solo puede contener números');
                return false;
            }
            if (documento.length < 6 || documento.length > 10) {
                mostrarError(campoDocumento, 'La cédula debe tener entre 6 y 10 dígitos');
                return false;
            }
            break;

        case 'TI': // Tarjeta de Identidad
            if (!REGEX.soloNumeros.test(documento)) {
                mostrarError(campoDocumento, 'La tarjeta de identidad solo puede contener números');
                return false;
            }
            if (documento.length < 8 || documento.length > 11) {
                mostrarError(campoDocumento, 'La tarjeta de identidad debe tener entre 8 y 11 dígitos');
                return false;
            }
            break;

        case 'NIT': // NIT para negocios
            if (!REGEX.soloNumeros.test(documento)) {
                mostrarError(campoDocumento, 'El NIT solo puede contener números');
                return false;
            }
            if (documento.length < 9 || documento.length > 10) {
                mostrarError(campoDocumento, 'El NIT debe tener entre 9 y 10 dígitos');
                return false;
            }
            break;

        case 'PASAPORTE':
            if (!REGEX.alfanumerico.test(documento)) {
                mostrarError(campoDocumento, 'El pasaporte solo puede contener letras y números');
                return false;
            }
            if (documento.length < 6 || documento.length > 12) {
                mostrarError(campoDocumento, 'El pasaporte debe tener entre 6 y 12 caracteres');
                return false;
            }
            break;

        default:
            mostrarError(campoDocumento, 'Primero selecciona un tipo de documento');
            return false;
    }

    mostrarValido(campoDocumento);
    return true;
}

/**
 * Validar rol seleccionado
 */
function validarRol(campo) {
    const rol = campo.value;

    if (rol === '' || rol === null) {
        mostrarError(campo, 'Debes seleccionar un rol para el usuario');
        return false;
    }

    mostrarValido(campo);
    return true;
}

// ========== FUNCIONALIDAD MOSTRAR/OCULTAR CONTRASEÑA ==========
function togglePasswordVisibility(buttonId, inputId) {
    const button = document.getElementById(buttonId);
    const input = document.getElementById(inputId);

    if (button && input) {
        button.addEventListener('click', function() {
            const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
            input.setAttribute('type', type);

            // Cambiar ícono
            const icon = this.querySelector('i');
            if (icon) {
                if (type === 'password') {
                    icon.classList.remove('bi-eye-slash');
                    icon.classList.add('bi-eye');
                } else {
                    icon.classList.remove('bi-eye');
                    icon.classList.add('bi-eye-slash');
                }
            }
        });
    }
}

// ========== INICIALIZACIÓN PARA LOGIN ==========
function initLoginValidations() {
    const form = document.querySelector('form[action*="/login"]');
    if (!form) return;

    const emailField = document.getElementById('username'); // Spring Security usa 'username'
    const passwordField = document.getElementById('password');

    // Validaciones en tiempo real
    if (emailField) {
        emailField.addEventListener('blur', () => validarEmail(emailField));
    }

    if (passwordField) {
        passwordField.addEventListener('blur', () => validarContrasena(passwordField, true));
    }

    // Validación al enviar
    form.addEventListener('submit', function(e) {
        let esValido = true;

        if (emailField && !validarEmail(emailField)) {
            esValido = false;
        }

        if (passwordField && !validarContrasena(passwordField, true)) {
            esValido = false;
        }

        if (!esValido) {
            e.preventDefault();

            // Hacer focus en el primer campo con error
            const primerError = form.querySelector('.is-invalid');
            if (primerError) {
                primerError.focus();
            }
        }
    });
}

// ========== INICIALIZACIÓN PARA REGISTRO ==========
function initRegisterValidations() {
    const form = document.querySelector('form[action*="/register"]');
    if (!form) return;

    const nombreField = document.getElementById('nombre');
    const emailField = document.getElementById('email');
    const contrasenaField = document.getElementById('contrasena');
    const telefonoField = document.getElementById('telefono');
    const tipoDocumentoField = document.getElementById('tipo_documento');
    const documentoField = document.getElementById('documento');
    const rolField = document.getElementById('rolSeleccionado');

    // Validaciones en tiempo real (blur)
    if (nombreField) {
        nombreField.addEventListener('blur', () => validarNombre(nombreField));
    }

    if (emailField) {
        emailField.addEventListener('blur', () => validarEmail(emailField));
    }

    if (contrasenaField) {
        contrasenaField.addEventListener('blur', () => validarContrasena(contrasenaField, true));
    }

    if (telefonoField) {
        telefonoField.addEventListener('blur', () => validarTelefono(telefonoField, true));
    }

    if (tipoDocumentoField) {
        tipoDocumentoField.addEventListener('change', () => {
            validarTipoDocumento(tipoDocumentoField);
            // Revalidar documento si ya fue ingresado
            if (documentoField && documentoField.value.trim() !== '') {
                validarDocumento(documentoField, tipoDocumentoField);
            }
        });
    }

    if (documentoField && tipoDocumentoField) {
        documentoField.addEventListener('blur', () => validarDocumento(documentoField, tipoDocumentoField));
    }

    if (rolField) {
        rolField.addEventListener('change', () => validarRol(rolField));
    }

    // Validación completa al enviar
    form.addEventListener('submit', function(e) {
        let esValido = true;

        if (nombreField && !validarNombre(nombreField)) {
            esValido = false;
        }

        if (emailField && !validarEmail(emailField)) {
            esValido = false;
        }

        if (contrasenaField && !validarContrasena(contrasenaField, true)) {
            esValido = false;
        }

        if (telefonoField && !validarTelefono(telefonoField, true)) {
            esValido = false;
        }

        if (tipoDocumentoField && !validarTipoDocumento(tipoDocumentoField)) {
            esValido = false;
        }

        if (documentoField && tipoDocumentoField && !validarDocumento(documentoField, tipoDocumentoField)) {
            esValido = false;
        }

        if (rolField && !validarRol(rolField)) {
            esValido = false;
        }

        if (!esValido) {
            e.preventDefault();

            // Hacer focus en el primer campo con error
            const primerError = form.querySelector('.is-invalid');
            if (primerError) {
                primerError.focus();
            }
        }
    });
}

// ========== INICIALIZACIÓN AUTOMÁTICA ==========
document.addEventListener('DOMContentLoaded', function() {
    // Detectar qué página es e inicializar las validaciones correspondientes
    if (window.location.pathname.includes('/login')) {
        initLoginValidations();
    } else if (window.location.pathname.includes('/register')) {
        initRegisterValidations();
    }
});