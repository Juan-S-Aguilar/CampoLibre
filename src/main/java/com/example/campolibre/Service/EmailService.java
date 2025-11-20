package com.example.campolibre.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from:${spring.mail.username:eventos@campolibre.com}}")
    private String fromAddress;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ✅ 1) Confirmación de cuenta
    @Async
    public void enviarConfirmacionCuenta(String emailUsuario, String nombreUsuario) {
        String asunto = "Cuenta creada en Campolibre: Bienvenido, " + nombreUsuario + "!";
        String cuerpo = String.format(
                "Hola %s,\n\n" +
                        "¡Gracias por registrarte en Campolibre! Tu cuenta ha sido creada correctamente.\n\n" +
                        "Puedes iniciar sesión con tu correo y contraseña.\n\n" +
                        "Saludos,\n" +
                        "El equipo de Campolibre",
                nombreUsuario
        );
        enviarCorreo(emailUsuario, asunto, cuerpo);
    }

    // ✅ 2) Consumidor guarda evento
    @Async
    public void enviarConfirmacionGuardadoEvento(String emailConsumidor, String nombreConsumidor,
                                                 String nombreEvento, String ubicacion, String fecha, String hora,
                                                 String patrocinador) {
        String asunto = "🎉 Evento Guardado: Confirmación y detalles de " + nombreEvento;
        String cuerpo = String.format(
                "Hola %s,\n\n" +
                        "¡Tu registro de interés para el evento **'%s'** ha sido confirmado!\n\n" +
                        "**Guarda este correo, ya que servirá como tu pase para registrar la asistencia** el día del evento.\n\n" +
                        "--- DETALLES DEL EVENTO ---\n" +
                        "**Evento:** %s\n" +
                        "**Patrocinador:** %s\n" +
                        "**Ubicación:** %s\n" +
                        "**Fecha:** %s a las %s\n" +
                        "--------------------------\n\n" +
                        "¡Esperamos verte allí!\n\n" +
                        "Saludos,\n" +
                        "El equipo de Campolibre",
                nombreConsumidor, nombreEvento, nombreEvento, patrocinador, ubicacion, fecha, hora
        );
        enviarCorreo(emailConsumidor, asunto, cuerpo);
    }

    // ✅ 3) Notificar a proveedores sobre evento publicado
    @Async
    public void enviarNotificacionEventoPublicado(String emailProveedor, String nombreProveedor,
                                                  String nombreEvento, String ubicacion,
                                                  String fecha, String hora,
                                                  Integer cuposDisponibles, Double costoEspacio) {
        String asunto = "🎉 Nuevo Evento Disponible: " + nombreEvento;
        String cuerpo = String.format(
                "Hola %s,\n\n" +
                        "¡Tenemos un nuevo evento disponible para proveedores!\n\n" +
                        "--- DETALLES DEL EVENTO ---\n" +
                        "**Evento:** %s\n" +
                        "**Ubicación:** %s\n" +
                        "**Fecha:** %s a las %s\n" +
                        "**Cupos Disponibles:** %d\n" +
                        "**Costo por Espacio:** $%.2f\n" +
                        "--------------------------\n\n" +
                        "Para inscribirte, ingresa a la plataforma y solicita tu cupo.\n\n" +
                        "¡No pierdas esta oportunidad!\n\n" +
                        "Saludos,\n" +
                        "El equipo de Campolibre",
                nombreProveedor, nombreEvento, ubicacion, fecha, hora,
                cuposDisponibles, costoEspacio
        );
        enviarCorreo(emailProveedor, asunto, cuerpo);
    }

    // ✅ 4) Confirmación de pago exitoso (NUEVO)
    @Async
    public void enviarConfirmacionPago(String emailProveedor, String nombreProveedor,
                                       String nombreEvento, String codigoConfirmacion,
                                       String numeroTransaccion, Double montoPagado) {
        String asunto = "✅ Pago Confirmado: Tu cupo en " + nombreEvento;
        String cuerpo = String.format(
                "Hola %s,\n\n" +
                        "¡Tu pago ha sido procesado exitosamente! Tu cupo en el evento está confirmado.\n\n" +
                        "--- DETALLES DEL PAGO ---\n" +
                        "**Evento:** %s\n" +
                        "**Monto Pagado:** $%,.2f COP\n" +
                        "**Número de Transacción:** %s\n" +
                        "**Código de Confirmación:** %s\n" +
                        "--------------------------\n\n" +
                        "**IMPORTANTE:** Guarda este código de confirmación.\n" +
                        "Deberás presentarlo el día del evento para validar tu entrada.\n\n" +
                        "**Próximos pasos:**\n" +
                        "1. Guarda este correo como comprobante\n" +
                        "2. Revisa los detalles del evento en tu perfil\n" +
                        "3. Prepara tu stand o espacio\n" +
                        "4. Presenta tu código de confirmación el día del evento\n\n" +
                        "¡Nos vemos en el evento!\n\n" +
                        "Saludos,\n" +
                        "El equipo de Campolibre",
                nombreProveedor, nombreEvento, montoPagado, numeroTransaccion, codigoConfirmacion
        );
        enviarCorreo(emailProveedor, asunto, cuerpo);
    }

    // ✅ 5) Notificación de pago fallido (NUEVO)
    @Async
    public void enviarNotificacionPagoFallido(String emailProveedor, String nombreProveedor,
                                              String nombreEvento, String numeroTransaccion,
                                              String motivoFallo) {
        String asunto = "❌ Pago No Procesado: " + nombreEvento;
        String cuerpo = String.format(
                "Hola %s,\n\n" +
                        "Lamentablemente, tu pago para el evento '%s' no pudo ser procesado.\n\n" +
                        "--- DETALLES ---\n" +
                        "**Número de Transacción:** %s\n" +
                        "**Motivo:** %s\n" +
                        "--------------------------\n\n" +
                        "**¿Qué puedes hacer?**\n" +
                        "1. Verifica que tu tarjeta tenga fondos suficientes\n" +
                        "2. Intenta con otro método de pago\n" +
                        "3. Contacta a tu banco si el problema persiste\n\n" +
                        "Puedes intentar nuevamente ingresando a tu perfil en la sección 'Mis Inscripciones'.\n\n" +
                        "Si necesitas ayuda, contáctanos a soporte@campolibre.com\n\n" +
                        "Saludos,\n" +
                        "El equipo de Campolibre",
                nombreProveedor, nombreEvento, numeroTransaccion,
                (motivoFallo != null && !motivoFallo.isEmpty()) ? motivoFallo : "Error en la pasarela de pagos"
        );
        enviarCorreo(emailProveedor, asunto, cuerpo);
    }

    // 🔧 Método genérico reutilizable
    private void enviarCorreo(String destinatario, String asunto, String cuerpo) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpo, false);

            System.out.println("[EmailService] Enviando correo: " + asunto + " -> " + destinatario);
            mailSender.send(mimeMessage);
            System.out.println("[EmailService] Correo enviado correctamente a " + destinatario);

        } catch (MailException me) {
            System.err.println("[EmailService] MailException al enviar a " + destinatario + ": " + me.getMessage());
            me.printStackTrace();
        } catch (Exception e) {
            System.err.println("[EmailService] Error al enviar correo a " + destinatario + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}