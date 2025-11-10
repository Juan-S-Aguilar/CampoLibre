package com.example.campolibre.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarInvitacion(String emailConsumidor, String nombreEvento) {
        // 1. Crear el mensaje
        SimpleMailMessage mensaje = new SimpleMailMessage();

        // El 'From' es opcional, pero ayuda a identificar quién envía el correo
        mensaje.setFrom("eventos@campolibre.com");

        // 2. Definir el destinatario (el consumidor que guardó el evento)
        mensaje.setTo(emailConsumidor);

        // 3. Definir el asunto
        mensaje.setSubject("🎉 ¡Tu Invitación a " + nombreEvento + " ha sido Confirmada!");

        // 4. Definir el cuerpo del mensaje
        String cuerpo = String.format(
                "Hola,\n\n" +
                        "¡Felicidades! Has guardado exitosamente el evento '%s' en Campolibre.\n\n" +
                        "Esperamos verte pronto. Puedes ver los detalles del evento aquí: [Enlace al Evento]\n\n" +
                        "Saludos,\n" +
                        "El equipo de Campolibre",
                nombreEvento
        );
        mensaje.setText(cuerpo);

        // 5. Enviar el mensaje
        try {
            mailSender.send(mensaje);
            System.out.println("Correo de invitación enviado con éxito a: " + emailConsumidor);
        } catch (Exception e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            // Manejo de errores (puedes loguear esto, pero el evento debe guardarse de todas formas)
        }
    }
}