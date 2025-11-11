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

    // ✅ 2) Enviar correo cuando confirma asistencia a un evento
    @Async
    public void enviarConfirmacionParticipacion(String emailConsumidor, String nombreEvento) {
        String asunto = "✅ Confirmación de asistencia: " + nombreEvento;
        String cuerpo = String.format(
                "Hola,\n\n" +
                        "Tu asistencia al evento '%s' ha sido confirmada. ¡Te esperamos!\n\n" +
                        "Si necesitas cancelar, puedes hacerlo desde tu perfil.\n\n" +
                        "Saludos,\n" +
                        "El equipo de Campolibre",
                nombreEvento
        );

        enviarCorreo(emailConsumidor, asunto, cuerpo);
    }

    // ✅ 3) Enviar invitación al evento (cuando confirma asistencia)
    @Async
    public void enviarInvitacion(String emailConsumidor, String nombreEvento) {
        String asunto = "🎉 Invitación confirmada: " + nombreEvento;
        String cuerpo = String.format(
                "Hola,\n\n" +
                        "Gracias por confirmar tu asistencia al evento '%s'.\n\n" +
                        "Aquí tienes tu invitación oficial al evento.\n" +
                        "Guarda este correo y preséntalo el día del evento si es necesario.\n\n" +
                        "¡Nos vemos pronto!\n\n" +
                        "Saludos,\n" +
                        "El equipo de Campolibre",
                nombreEvento
        );

        enviarCorreo(emailConsumidor, asunto, cuerpo);
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
}