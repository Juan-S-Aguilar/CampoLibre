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

    // Dirección del remitente (usa la configurada o una predeterminada)
    @Value("${mail.from:${spring.mail.username:eventos@campolibre.com}}")
    private String fromAddress;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ✅ 1) Enviar correo al crear cuenta
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

    // 🔧 Método genérico reutilizable (centraliza el envío de correos)
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

    // Modificación sugerida para enviar un solo correo informativo

    // ✅ 2) Enviar correo cuando el consumidor GUARDA un evento (Intención de Asistencia)
    @Async
    public void enviarConfirmacionGuardadoEvento(String emailConsumidor, String nombreConsumidor,
                                                 String nombreEvento, String ubicacion, String fecha, String hora,
                                                 String patrocinador) {
        String asunto = "🎉 Evento Guardado: Confirmación y detalles de " + nombreEvento;

        // Crear un cuerpo de correo más detallado que sirva como "pase"
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
}