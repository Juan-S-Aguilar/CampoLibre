/**
 * ============================================================
 * VALIDACIONES PARA MÓDULO USUARIO - CAMPO LIBRE
 * ============================================================
 * Archivo: validaciones-usuario.js
 * Uso: Usuario Create (form.html) y Edit (edit.html)
 * ============================================================
 */

// ========== CONFIGURACIÓN GENERAL ==========
const CONFIG_USUARIO = {
    PASSWORD_MIN_LENGTH: 6,
    TELEFONO_LENGTH: 10,
    NOMBRE_MIN_LENGTH: 3,
    NOMBRE_MAX_LENGTH: 100,
    EMAIL_MAX_LENGTH: 100,
    DOCUMENTO_MIN_LENGTH: 5,
    DOCUMENTO_MAX_LENGTH: 20
};

// ========== EXPRESIONES REGULARES ==========
const REGEX_USUARIO = {
    email: /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
    soloNumeros: /^[0-9]+$/,
    soloLetras: /^[a-záéíóúñA-ZÁÉÍÓÚÑ\s]+$/,
    alfanumerico: /^[a-zA-Z0-9]+$/
};

// ========== UTILIDADES DE FEEDBACK VISUAL ==========
function mostrarErrorUsuario(campo, mensaje) {
    const formGroup = campo.closest('.form-group') || campo.closest('.mb-3');

    campo.classList.add('is-invalid');
    campo.classList.remove('is-valid');

    let feedback = formGroup.querySelector('.invalid-feedback');
    if (!feedback) {
        feedback = document.createElement('div');
        feedback.className = 'invalid-feedback';
        feedback.style.display = 'block';
        campo.parentNode.insertBefore(feedback, campo.nextSibling);
    }

    feedback.textContent = mensaje;
    feedback.style.display = 'block';
}

function mostrarValidoUsuario(campo) {
    const formGroup = campo.closest('.form-group') || campo.closest('.mb-3');

    campo.classList.remove('is-invalid');
    campo.classList.add('is-valid');

    const feedback = formGroup.querySelector('.invalid-feedback');
    if (feedback) {
        feedback.style.display = 'none';
    }
}

function limpiarValidacionUsuario(campo) {
    campo.classList.remove('is-invalid', 'is-valid');

    const formGroup = campo.closest('.form-group') || campo.closest('.mb-3');
    const feedback = formGroup.querySelector('.invalid-feedback');
    if (feedback) {
        feedback.style.display = 'none';
    }
}

// ========== VALIDACIONES INDIVIDUALES ==========

function validarNombreUsuario(campo) {
    const nombre = campo.value.trim();

    if (nombre === '') {
        mostrarErrorUsuario(campo, 'El nombre completo es obligatorio');
        return false;
    }

    if (nombre.length < CONFIG_USUARIO.NOMBRE_MIN_LENGTH) {
        mostrarErrorUsuario(campo, `El nombre debe tener mínimo ${CONFIG_USUARIO.NOMBRE_MIN_LENGTH} caracteres`);
        return false;
    }

    if (nombre.length > CONFIG_USUARIO.NOMBRE_MAX_LENGTH) {
        mostrarErrorUsuario(campo, `El nombre no puede superar ${CONFIG_USUARIO.NOMBRE_MAX_LENGTH} caracteres`);
        return false;
    }

    if (!REGEX_USUARIO.soloLetras.test(nombre)) {
        mostrarErrorUsuario(campo, 'El nombre solo puede contener letras y espacios');
        return false;
    }

    if (nombre.replace(/\s/g, '').length === 0) {
        mostrarErrorUsuario(campo, 'El nombre no puede contener solo espacios');
        return false;
    }

    mostrarValidoUsuario(campo);
    return true;
}

function validarEmailUsuario(campo) {
    const email = campo.value.trim();

    if (email === '') {
        mostrarErrorUsuario(campo, 'El correo electrónico es obligatorio');
        return false;
    }

    if (email.length > CONFIG_USUARIO.EMAIL_MAX_LENGTH) {
        mostrarErrorUsuario(campo, `El correo no puede superar ${CONFIG_USUARIO.EMAIL_MAX_LENGTH} caracteres`);
        return false;
    }

    if (!REGEX_USUARIO.email.test(email)) {
        mostrarErrorUsuario(campo, 'Ingresa un correo electrónico válido (ejemplo: usuario@dominio.com)');
        return false;
    }

    mostrarValidoUsuario(campo);
    return true;
}

function validarContrasenaUsuario(campo, esObligatorio = true) {
    const contrasena = campo.value;

    // Si está vacío y NO es obligatorio (edición)
    if (contrasena === '' && !esObligatorio) {
        limpiarValidacionUsuario(campo);
        return true;
    }

    if (contrasena === '' && esObligatorio) {
        mostrarErrorUsuario(campo, 'La contraseña es obligatoria');
        return false;
    }

    if (contrasena.length < CONFIG_USUARIO.PASSWORD_MIN_LENGTH) {
        mostrarErrorUsuario(campo, `La contraseña debe tener mínimo ${CONFIG_USUARIO.PASSWORD_MIN_LENGTH} caracteres`);
        return false;
    }

    mostrarValidoUsuario(campo);
    return true;
}

function validarTelefonoUsuario(campo, esObligatorio = false) {
    const telefono = campo.value.trim();

    if (telefono === '' && !esObligatorio) {
        limpiarValidacionUsuario(campo);
        return true;
    }

    if (telefono === '' && esObligatorio) {
        mostrarErrorUsuario(campo, 'El teléfono es obligatorio');
        return false;
    }

    if (!REGEX_USUARIO.soloNumeros.test(telefono)) {
        mostrarErrorUsuario(campo, 'El teléfono solo puede contener números');
        return false;
    }

    if (telefono.length !== CONFIG_USUARIO.TELEFONO_LENGTH) {
        mostrarErrorUsuario(campo, `El teléfono debe tener exactamente ${CONFIG_USUARIO.TELEFONO_LENGTH} dígitos`);
        return false;
    }

    if (!telefono.startsWith('3')) {
        mostrarErrorUsuario(campo, 'El teléfono móvil debe iniciar con 3 (Ej: 3001234567)');
        return false;
    }

    mostrarValidoUsuario(campo);
    return true;
}

function validarTipoDocumentoUsuario(campo) {
    const tipoDocumento = campo.value;

    if (tipoDocumento === '' || tipoDocumento === null) {
        mostrarErrorUsuario(campo, 'Debes seleccionar un tipo de documento');
        return false;
    }

    mostrarValidoUsuario(campo);
    return true;
}

function validarDocumentoUsuario(campoDocumento, campoTipoDocumento) {
    const documento = campoDocumento.value.trim();
    const tipoDocumento = campoTipoDocumento.value;

    if (documento === '') {
        mostrarErrorUsuario(campoDocumento, 'El número de documento es obligatorio');
        return false;
    }

    switch(tipoDocumento) {
        case 'CC':
        case 'CE':
            if (!REGEX_USUARIO.soloNumeros.test(documento)) {
                mostrarErrorUsuario(campoDocumento, 'La cédula solo puede contener números');
                return false;
            }
            if (documento.length < 6 || documento.length > 10) {
                mostrarErrorUsuario(campoDocumento, 'La cédula debe tener entre 6 y 10 dígitos');
                return false;
            }
            break;

        case 'TI':
            if (!REGEX_USUARIO.soloNumeros.test(documento)) {
                mostrarErrorUsuario(campoDocumento, 'La tarjeta de identidad solo puede contener números');
                return false;
            }
            if (documento.length < 8 || documento.length > 11) {
                mostrarErrorUsuario(campoDocumento, 'La tarjeta de identidad debe tener entre 8 y 11 dígitos');
                return false;
            }
            break;

        case 'NIT':
            if (!REGEX_USUARIO.soloNumeros.test(documento)) {
                mostrarErrorUsuario(campoDocumento, 'El NIT solo puede contener números');
                return false;
            }
            if (documento.length < 9 || documento.length > 10) {
                mostrarErrorUsuario(campoDocumento, 'El NIT debe tener entre 9 y 10 dígitos');
                return false;
            }
            break;

        case 'PASAPORTE':
            if (!REGEX_USUARIO.alfanumerico.test(documento)) {
                mostrarErrorUsuario(campoDocumento, 'El pasaporte solo puede contener letras y números');
                return false;
            }
            if (documento.length < 6 || documento.length > 12) {
                mostrarErrorUsuario(campoDocumento, 'El pasaporte debe tener entre 6 y 12 caracteres');
                return false;
            }
            break;

        default:
            mostrarErrorUsuario(campoDocumento, 'Primero selecciona un tipo de documento');
            return false;
    }

    mostrarValidoUsuario(campoDocumento);
    return true;
}

function validarRolUsuario(campo) {
    const rol = campo.value;

    if (rol === '' || rol === null) {
        mostrarErrorUsuario(campo, 'Debes seleccionar un rol para el usuario');
        return false;
    }

    mostrarValidoUsuario(campo);
    return true;
}

// ========== INICIALIZACIÓN PARA CREAR USUARIO ==========
function initUsuarioFormValidations() {
    const form = document.querySelector('form[action*="/usuarios/crear"]');
    if (!form) return;

    const nombreField = document.getElementById('nombre');
    const emailField = document.getElementById('email');
    const contrasenaField = document.getElementById('contrasena');
    const telefonoField = document.getElementById('telefono');
    const tipoDocumentoField = document.getElementById('tipo_documento');
    const documentoField = document.getElementById('documento');
    const rolField = document.getElementById('id_rol');

    // Validaciones en tiempo real
    if (nombreField) {
        nombreField.addEventListener('blur', () => validarNombreUsuario(nombreField));
    }

    if (emailField) {
        emailField.addEventListener('blur', () => validarEmailUsuario(emailField));
    }

    if (contrasenaField) {
        contrasenaField.addEventListener('blur', () => validarContrasenaUsuario(contrasenaField, true));
    }

    if (telefonoField) {
        telefonoField.addEventListener('blur', () => validarTelefonoUsuario(telefonoField, false));
    }

    if (tipoDocumentoField) {
        tipoDocumentoField.addEventListener('change', () => {
            validarTipoDocumentoUsuario(tipoDocumentoField);
            if (documentoField && documentoField.value.trim() !== '') {
                validarDocumentoUsuario(documentoField, tipoDocumentoField);
            }
        });
    }

    if (documentoField && tipoDocumentoField) {
        documentoField.addEventListener('blur', () => validarDocumentoUsuario(documentoField, tipoDocumentoField));
    }

    if (rolField) {
        rolField.addEventListener('change', () => validarRolUsuario(rolField));
    }

    // Validación al enviar
    form.addEventListener('submit', function(e) {
        let esValido = true;

        if (nombreField && !validarNombreUsuario(nombreField)) {
            esValido = false;
        }

        if (emailField && !validarEmailUsuario(emailField)) {
            esValido = false;
        }

        if (contrasenaField && !validarContrasenaUsuario(contrasenaField, true)) {
            esValido = false;
        }

        if (telefonoField && telefonoField.value.trim() !== '' && !validarTelefonoUsuario(telefonoField, false)) {
            esValido = false;
        }

        if (tipoDocumentoField && !validarTipoDocumentoUsuario(tipoDocumentoField)) {
            esValido = false;
        }

        if (documentoField && tipoDocumentoField && !validarDocumentoUsuario(documentoField, tipoDocumentoField)) {
            esValido = false;
        }

        if (rolField && !validarRolUsuario(rolField)) {
            esValido = false;
        }

        if (!esValido) {
            e.preventDefault();

            const primerError = form.querySelector('.is-invalid');
            if (primerError) {
                primerError.focus();
            }
        }
    });
}

// ========== INICIALIZACIÓN PARA EDITAR USUARIO ==========
function initUsuarioEditValidations() {
    const form = document.querySelector('form[action*="/usuarios/actualizar"]');
    if (!form) return;

    const nombreField = document.getElementById('nombre');
    const emailField = document.getElementById('email');
    const contrasenaField = document.getElementById('contrasena');
    const telefonoField = document.getElementById('telefono');
    const rolField = document.getElementById('id_rol');

    // Validaciones en tiempo real
    if (nombreField) {
        nombreField.addEventListener('blur', () => validarNombreUsuario(nombreField));
    }

    // Email es readonly en edición, pero podemos validar por si acaso
    if (emailField && !emailField.hasAttribute('readonly')) {
        emailField.addEventListener('blur', () => validarEmailUsuario(emailField));
    }

    // Contraseña es OPCIONAL en edición
    if (contrasenaField) {
        contrasenaField.addEventListener('blur', () => validarContrasenaUsuario(contrasenaField, false));
    }

    if (telefonoField) {
        telefonoField.addEventListener('blur', () => validarTelefonoUsuario(telefonoField, false));
    }

    if (rolField) {
        rolField.addEventListener('change', () => validarRolUsuario(rolField));
    }

    // Validación al enviar
    form.addEventListener('submit', function(e) {
        let esValido = true;

        if (nombreField && !validarNombreUsuario(nombreField)) {
            esValido = false;
        }

        // Validar contraseña SOLO si se ingresó algo
        if (contrasenaField && contrasenaField.value.trim() !== '') {
            if (!validarContrasenaUsuario(contrasenaField, false)) {
                esValido = false;
            }
        }

        // Validar teléfono SOLO si se ingresó algo
        if (telefonoField && telefonoField.value.trim() !== '') {
            if (!validarTelefonoUsuario(telefonoField, false)) {
                esValido = false;
            }
        }

        if (rolField && !validarRolUsuario(rolField)) {
            esValido = false;
        }

        if (!esValido) {
            e.preventDefault();

            const primerError = form.querySelector('.is-invalid');
            if (primerError) {
                primerError.focus();
            }
        }
    });
}

// ========== FUNCIONALIDAD MOSTRAR/OCULTAR CONTRASEÑA ==========
function initTogglePassword() {
    // Buscar campos de contraseña
    const passwordFields = document.querySelectorAll('input[type="password"]');

    passwordFields.forEach(field => {
        const wrapper = field.parentElement;

        // Solo agregar botón si no existe ya
        if (!wrapper.querySelector('.toggle-password')) {
            // Crear botón para mostrar/ocultar
            const toggleButton = document.createElement('button');
            toggleButton.type = 'button';
            toggleButton.className = 'toggle-password';
            toggleButton.style.cssText = 'position: absolute; right: 10px; top: 50%; transform: translateY(-50%); border: none; background: transparent; cursor: pointer;';
            toggleButton.innerHTML = '<i class="bi bi-eye"></i>';

            // Hacer el wrapper relativo
            wrapper.style.position = 'relative';
            wrapper.appendChild(toggleButton);

            // Funcionalidad toggle
            toggleButton.addEventListener('click', function() {
                const type = field.getAttribute('type') === 'password' ? 'text' : 'password';
                field.setAttribute('type', type);

                const icon = this.querySelector('i');
                if (type === 'password') {
                    icon.classList.remove('bi-eye-slash');
                    icon.classList.add('bi-eye');
                } else {
                    icon.classList.remove('bi-eye');
                    icon.classList.add('bi-eye-slash');
                }
            });
        }
    });
}

// ========== INICIALIZACIÓN AUTOMÁTICA ==========
document.addEventListener('DOMContentLoaded', function() {
    // Detectar qué formulario es
    if (window.location.pathname.includes('/usuarios/crear') ||
        document.querySelector('form[action*="/usuarios/crear"]')) {
        initUsuarioFormValidations();
    } else if (window.location.pathname.includes('/usuarios/editar') ||
               document.querySelector('form[action*="/usuarios/actualizar"]')) {
        initUsuarioEditValidations();
    }

    // Inicializar toggle de contraseña en ambos casos
    initTogglePassword();
});