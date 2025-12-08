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
        String asunto = "Bienvenido a Campolibre, " + nombreUsuario + "!";
        String cuerpo = String.format("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #f4f4f4;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background-color: #ffffff;
                        }
                        .header {
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            padding: 40px 20px;
                            text-align: center;
                            color: white;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 600;
                        }
                        .content {
                            padding: 40px 30px;
                            color: #333;
                            line-height: 1.6;
                        }
                        .welcome-icon {
                            font-size: 60px;
                            text-align: center;
                            margin: 20px 0;
                        }
                        .button {
                            display: inline-block;
                            padding: 12px 30px;
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            color: white;
                            text-decoration: none;
                            border-radius: 25px;
                            margin: 20px 0;
                            font-weight: 600;
                        }
                        .features {
                            background-color: #f8f9fa;
                            padding: 20px;
                            border-radius: 10px;
                            margin: 20px 0;
                        }
                        .feature-item {
                            padding: 10px 0;
                            border-left: 4px solid #667eea;
                            padding-left: 15px;
                            margin: 10px 0;
                        }
                        .footer {
                            background-color: #2d3748;
                            color: #a0aec0;
                            padding: 30px;
                            text-align: center;
                            font-size: 14px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 Bienvenido a Campolibre</h1>
                        </div>

                        <div class="content">
                            <div class="welcome-icon">👋</div>

                            <h2 style="color: #667eea;">¡Hola %s!</h2>

                            <p style="font-size: 16px;">
                                ¡Gracias por registrarte en <strong>Campolibre</strong>! Estamos emocionados de tenerte como parte de nuestra comunidad.
                            </p>

                            <p>
                                Tu cuenta ha sido creada exitosamente y ya puedes comenzar a disfrutar de todos nuestros servicios.
                            </p>

                            <div class="features">
                                <h3 style="color: #667eea; margin-top: 0;">¿Qué puedes hacer ahora?</h3>
                                <div class="feature-item">
                                    <strong>📅 Descubre eventos</strong> - Explora todos los eventos disponibles en tu área
                                </div>
                                <div class="feature-item">
                                    <strong>🎫 Registra tu asistencia</strong> - Confirma tu participación en los eventos que te interesen
                                </div>
                                <div class="feature-item">
                                    <strong>👤 Completa tu perfil</strong> - Personaliza tu experiencia en la plataforma
                                </div>
                            </div>

                            <div style="text-align: center; margin: 30px 0;">
                                <p><strong>Accede con tu correo y contraseña:</strong></p>
                                <p style="background-color: #f1f5f9; padding: 10px; border-radius: 5px; font-family: monospace;">
                                    %s
                                </p>
                            </div>

                            <p style="margin-top: 30px;">
                                Si tienes alguna pregunta, no dudes en contactarnos. ¡Estamos aquí para ayudarte!
                            </p>
                        </div>

                        <div class="footer">
                            <p><strong>Campolibre</strong></p>
                            <p>Tu plataforma de eventos favorita</p>
                            <p style="margin-top: 15px; font-size: 12px;">
                                © 2024 Campolibre. Todos los derechos reservados.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                nombreUsuario, emailUsuario
        );
        enviarCorreo(emailUsuario, asunto, cuerpo);
    }

    // ✅ 2) Consumidor guarda evento
    @Async
    public void enviarConfirmacionGuardadoEvento(String emailConsumidor, String nombreConsumidor,
                                                 String nombreEvento, String ubicacion, String fecha, String hora,
                                                 String patrocinador) {
        String asunto = "¡Confirmación de asistencia a " + nombreEvento + "!";
        String cuerpo = String.format("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #f4f4f4;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background-color: #ffffff;
                        }
                        .header {
                            background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%);
                            padding: 40px 20px;
                            text-align: center;
                            color: white;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 600;
                        }
                        .content {
                            padding: 40px 30px;
                            color: #333;
                            line-height: 1.6;
                        }
                        .event-icon {
                            font-size: 60px;
                            text-align: center;
                            margin: 20px 0;
                        }
                        .ticket-box {
                            background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%);
                            color: white;
                            padding: 25px;
                            border-radius: 15px;
                            margin: 25px 0;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        }
                        .ticket-box h2 {
                            margin: 0 0 15px 0;
                            font-size: 24px;
                        }
                        .event-details {
                            background-color: #f8f9fa;
                            padding: 20px;
                            border-radius: 10px;
                            margin: 20px 0;
                        }
                        .detail-row {
                            display: flex;
                            padding: 12px 0;
                            border-bottom: 1px solid #e2e8f0;
                        }
                        .detail-row:last-child {
                            border-bottom: none;
                        }
                        .detail-label {
                            font-weight: 600;
                            color: #f5576c;
                            min-width: 130px;
                        }
                        .detail-value {
                            color: #333;
                        }
                        .alert-box {
                            background-color: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 5px;
                        }
                        .footer {
                            background-color: #2d3748;
                            color: #a0aec0;
                            padding: 30px;
                            text-align: center;
                            font-size: 14px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 ¡Asistencia Confirmada!</h1>
                        </div>

                        <div class="content">
                            <div class="event-icon">🎫</div>

                            <h2 style="color: #f5576c;">¡Hola %s!</h2>

                            <p style="font-size: 16px;">
                                Tu registro de interés para el evento <strong>"%s"</strong> ha sido confirmado exitosamente.
                            </p>

                            <div class="alert-box">
                                <strong>⚠️ IMPORTANTE:</strong> Guarda este correo, ya que servirá como tu <strong>pase digital</strong> para registrar la asistencia el día del evento.
                            </div>

                            <div class="ticket-box">
                                <h2>📋 Detalles del Evento</h2>
                                <p style="margin: 5px 0; opacity: 0.9;">Tu entrada confirmada para:</p>
                                <p style="font-size: 20px; margin: 10px 0 0 0; font-weight: 600;">%s</p>
                            </div>

                            <div class="event-details">
                                <div class="detail-row">
                                    <div class="detail-label">🏢 Patrocinador:</div>
                                    <div class="detail-value">%s</div>
                                </div>
                                <div class="detail-row">
                                    <div class="detail-label">📍 Ubicación:</div>
                                    <div class="detail-value">%s</div>
                                </div>
                                <div class="detail-row">
                                    <div class="detail-label">📅 Fecha:</div>
                                    <div class="detail-value">%s</div>
                                </div>
                                <div class="detail-row">
                                    <div class="detail-label">🕐 Hora:</div>
                                    <div class="detail-value">%s</div>
                                </div>
                            </div>

                            <div style="background-color: #e6f7ff; border-left: 4px solid #1890ff; padding: 15px; margin: 20px 0; border-radius: 5px;">
                                <p style="margin: 0;"><strong>💡 Consejo:</strong> Te recomendamos llegar con 15 minutos de anticipación para facilitar tu registro de entrada.</p>
                            </div>

                            <p style="text-align: center; margin-top: 30px; font-size: 18px; color: #f5576c; font-weight: 600;">
                                ¡Esperamos verte allí! 🎊
                            </p>
                        </div>

                        <div class="footer">
                            <p><strong>Campolibre</strong></p>
                            <p>Conectando comunidades a través de eventos</p>
                            <p style="margin-top: 15px; font-size: 12px;">
                                © 2024 Campolibre. Todos los derechos reservados.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """,
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
        String asunto = "Nuevo evento disponible: " + nombreEvento;
        String cuerpo = String.format("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #f4f4f4;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background-color: #ffffff;
                        }
                        .header {
                            background: linear-gradient(135deg, #43e97b 0%%, #38f9d7 100%%);
                            padding: 40px 20px;
                            text-align: center;
                            color: white;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 600;
                        }
                        .content {
                            padding: 40px 30px;
                            color: #333;
                            line-height: 1.6;
                        }
                        .event-icon {
                            font-size: 60px;
                            text-align: center;
                            margin: 20px 0;
                        }
                        .event-card {
                            background: linear-gradient(135deg, #43e97b 0%%, #38f9d7 100%%);
                            color: white;
                            padding: 25px;
                            border-radius: 15px;
                            margin: 25px 0;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        }
                        .event-card h2 {
                            margin: 0 0 10px 0;
                            font-size: 24px;
                        }
                        .info-grid {
                            display: grid;
                            gap: 15px;
                            margin: 20px 0;
                        }
                        .info-item {
                            background-color: #f8f9fa;
                            padding: 15px;
                            border-radius: 8px;
                            border-left: 4px solid #43e97b;
                        }
                        .info-label {
                            font-size: 12px;
                            color: #666;
                            text-transform: uppercase;
                            letter-spacing: 0.5px;
                            margin-bottom: 5px;
                        }
                        .info-value {
                            font-size: 16px;
                            color: #333;
                            font-weight: 600;
                        }
                        .highlight-box {
                            background-color: #fff3cd;
                            border: 2px solid #ffc107;
                            padding: 20px;
                            border-radius: 10px;
                            margin: 25px 0;
                            text-align: center;
                        }
                        .price-tag {
                            font-size: 32px;
                            color: #43e97b;
                            font-weight: 700;
                            margin: 10px 0;
                        }
                        .cta-button {
                            display: inline-block;
                            padding: 15px 40px;
                            background: linear-gradient(135deg, #43e97b 0%%, #38f9d7 100%%);
                            color: white;
                            text-decoration: none;
                            border-radius: 30px;
                            font-weight: 600;
                            font-size: 16px;
                            margin: 20px 0;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        }
                        .footer {
                            background-color: #2d3748;
                            color: #a0aec0;
                            padding: 30px;
                            text-align: center;
                            font-size: 14px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎪 Nuevo Evento Disponible</h1>
                        </div>

                        <div class="content">
                            <div class="event-icon">🎯</div>

                            <h2 style="color: #43e97b;">¡Hola %s!</h2>

                            <p style="font-size: 16px;">
                                ¡Tenemos excelentes noticias! Se ha publicado un <strong>nuevo evento</strong> donde puedes participar como proveedor.
                            </p>

                            <div class="event-card">
                                <h2>📅 %s</h2>
                                <p style="margin: 5px 0; opacity: 0.9;">¡Reserva tu espacio ahora!</p>
                            </div>

                            <div class="info-grid">
                                <div class="info-item">
                                    <div class="info-label">📍 Ubicación</div>
                                    <div class="info-value">%s</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">📅 Fecha y Hora</div>
                                    <div class="info-value">%s a las %s</div>
                                </div>
                                <div class="info-item">
                                    <div class="info-label">🎪 Cupos Disponibles</div>
                                    <div class="info-value">%d espacios</div>
                                </div>
                            </div>

                            <div class="highlight-box">
                                <p style="margin: 0 0 10px 0; font-size: 14px; color: #666;">
                                    <strong>💰 Inversión por Espacio</strong>
                                </p>
                                <div class="price-tag">$%,.2f COP</div>
                                <p style="margin: 10px 0 0 0; font-size: 12px; color: #666;">
                                    Pago único por todo el evento
                                </p>
                            </div>

                            <div style="background-color: #e6f7ff; border-left: 4px solid #1890ff; padding: 15px; margin: 20px 0; border-radius: 5px;">
                                <p style="margin: 0;"><strong>⚡ ¡Actúa rápido!</strong> Los cupos son limitados y se asignan por orden de pago confirmado.</p>
                            </div>

                            <div style="text-align: center; margin: 30px 0;">
                                <p style="font-size: 18px; color: #43e97b; font-weight: 600; margin-bottom: 15px;">
                                    ¿Listo para aprovechar esta oportunidad?
                                </p>
                                <p style="color: #666;">
                                    Ingresa a la plataforma de Campolibre y solicita tu cupo antes de que se agoten.
                                </p>
                            </div>

                            <div style="background-color: #f8f9fa; padding: 20px; border-radius: 10px; margin: 20px 0;">
                                <h3 style="color: #43e97b; margin-top: 0;">📝 Próximos pasos:</h3>
                                <ol style="margin: 10px 0; padding-left: 20px;">
                                    <li>Ingresa a tu cuenta en Campolibre</li>
                                    <li>Revisa los detalles completos del evento</li>
                                    <li>Solicita tu cupo</li>
                                    <li>Realiza el pago para confirmar tu participación</li>
                                </ol>
                            </div>
                        </div>

                        <div class="footer">
                            <p><strong>Campolibre</strong></p>
                            <p>Creando oportunidades para emprendedores</p>
                            <p style="margin-top: 15px; font-size: 12px;">
                                © 2024 Campolibre. Todos los derechos reservados.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                nombreProveedor, nombreEvento, ubicacion, fecha, hora, cuposDisponibles, costoEspacio
        );
        enviarCorreo(emailProveedor, asunto, cuerpo);
    }

    // ✅ 4) Confirmación de pago exitoso (NUEVO)
    @Async
    public void enviarConfirmacionPago(String emailProveedor, String nombreProveedor,
                                       String nombreEvento, String codigoConfirmacion,
                                       String numeroTransaccion, Double montoPagado) {
        String asunto = "Pago confirmado - Tu cupo en " + nombreEvento;
        String cuerpo = String.format("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #f4f4f4;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background-color: #ffffff;
                        }
                        .header {
                            background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%);
                            padding: 40px 20px;
                            text-align: center;
                            color: white;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 600;
                        }
                        .success-icon {
                            font-size: 80px;
                            text-align: center;
                            margin: 20px 0;
                        }
                        .content {
                            padding: 40px 30px;
                            color: #333;
                            line-height: 1.6;
                        }
                        .confirmation-box {
                            background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%);
                            color: white;
                            padding: 30px;
                            border-radius: 15px;
                            margin: 25px 0;
                            text-align: center;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        }
                        .confirmation-code {
                            font-size: 36px;
                            font-weight: 700;
                            letter-spacing: 4px;
                            margin: 15px 0;
                            padding: 15px;
                            background-color: rgba(255,255,255,0.2);
                            border-radius: 10px;
                            font-family: 'Courier New', monospace;
                        }
                        .payment-details {
                            background-color: #f8f9fa;
                            padding: 20px;
                            border-radius: 10px;
                            margin: 20px 0;
                        }
                        .detail-row {
                            display: flex;
                            justify-content: space-between;
                            padding: 12px 0;
                            border-bottom: 1px solid #e2e8f0;
                        }
                        .detail-row:last-child {
                            border-bottom: none;
                        }
                        .detail-label {
                            font-weight: 600;
                            color: #666;
                        }
                        .detail-value {
                            color: #333;
                            font-weight: 600;
                        }
                        .amount {
                            font-size: 28px;
                            color: #11998e;
                            font-weight: 700;
                        }
                        .steps-box {
                            background-color: #e6f7ff;
                            padding: 20px;
                            border-radius: 10px;
                            margin: 25px 0;
                        }
                        .step-item {
                            padding: 10px 0;
                            padding-left: 30px;
                            position: relative;
                        }
                        .step-item:before {
                            content: "✓";
                            position: absolute;
                            left: 0;
                            color: #11998e;
                            font-weight: 700;
                            font-size: 18px;
                        }
                        .alert-box {
                            background-color: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 5px;
                        }
                        .footer {
                            background-color: #2d3748;
                            color: #a0aec0;
                            padding: 30px;
                            text-align: center;
                            font-size: 14px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✅ Pago Confirmado</h1>
                        </div>

                        <div class="content">
                            <div class="success-icon">🎉</div>

                            <h2 style="color: #11998e; text-align: center;">¡Felicidades %s!</h2>

                            <p style="font-size: 16px; text-align: center;">
                                Tu pago ha sido procesado exitosamente. Tu cupo en el evento <strong>"%s"</strong> está confirmado.
                            </p>

                            <div class="confirmation-box">
                                <p style="margin: 0; font-size: 16px; opacity: 0.9;">
                                    Código de Confirmación
                                </p>
                                <div class="confirmation-code">%s</div>
                                <p style="margin: 10px 0 0 0; font-size: 14px; opacity: 0.9;">
                                    Presenta este código el día del evento
                                </p>
                            </div>

                            <div class="alert-box">
                                <strong>⚠️ IMPORTANTE:</strong> Guarda este correo en un lugar seguro. Necesitarás el código de confirmación para validar tu entrada el día del evento.
                            </div>

                            <div class="payment-details">
                                <h3 style="color: #11998e; margin-top: 0;">💳 Resumen de Pago</h3>
                                <div class="detail-row">
                                    <div class="detail-label">Evento:</div>
                                    <div class="detail-value">%s</div>
                                </div>
                                <div class="detail-row">
                                    <div class="detail-label">Monto Pagado:</div>
                                    <div class="detail-value amount">$%,.2f COP</div>
                                </div>
                                <div class="detail-row">
                                    <div class="detail-label">N° Transacción:</div>
                                    <div class="detail-value" style="font-family: monospace;">%s</div>
                                </div>
                            </div>

                            <div class="steps-box">
                                <h3 style="color: #11998e; margin-top: 0;">📝 Próximos Pasos</h3>
                                <div class="step-item">Guarda este correo como comprobante de pago</div>
                                <div class="step-item">Revisa los detalles del evento en tu perfil</div>
                                <div class="step-item">Prepara tu stand o espacio con anticipación</div>
                                <div class="step-item">Presenta tu código de confirmación el día del evento</div>
                            </div>

                            <div style="background-color: #f1f5f9; padding: 20px; border-radius: 10px; margin: 25px 0; text-align: center;">
                                <p style="margin: 0; color: #666;">
                                    Si tienes alguna pregunta o necesitas ayuda, no dudes en contactarnos. ¡Estamos aquí para ayudarte a tener una experiencia exitosa!
                                </p>
                            </div>

                            <p style="text-align: center; margin-top: 30px; font-size: 18px; color: #11998e; font-weight: 600;">
                                ¡Nos vemos en el evento! 🚀
                            </p>
                        </div>

                        <div class="footer">
                            <p><strong>Campolibre</strong></p>
                            <p>Impulsando el éxito de emprendedores</p>
                            <p style="margin-top: 15px; font-size: 12px;">
                                © 2024 Campolibre. Todos los derechos reservados.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                nombreProveedor, nombreEvento, codigoConfirmacion, nombreEvento, montoPagado, numeroTransaccion
        );
        enviarCorreo(emailProveedor, asunto, cuerpo);
    }

    // ✅ 5) Notificación de pago fallido (NUEVO)
    @Async
    public void enviarNotificacionPagoFallido(String emailProveedor, String nombreProveedor,
                                              String nombreEvento, String numeroTransaccion,
                                              String motivoFallo) {
        String asunto = "Pago no procesado - " + nombreEvento;
        String cuerpo = String.format("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #f4f4f4;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background-color: #ffffff;
                        }
                        .header {
                            background: linear-gradient(135deg, #eb3349 0%%, #f45c43 100%%);
                            padding: 40px 20px;
                            text-align: center;
                            color: white;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                            font-weight: 600;
                        }
                        .content {
                            padding: 40px 30px;
                            color: #333;
                            line-height: 1.6;
                        }
                        .error-icon {
                            font-size: 80px;
                            text-align: center;
                            margin: 20px 0;
                        }
                        .error-box {
                            background-color: #fee;
                            border-left: 4px solid #eb3349;
                            padding: 20px;
                            margin: 25px 0;
                            border-radius: 5px;
                        }
                        .transaction-details {
                            background-color: #f8f9fa;
                            padding: 20px;
                            border-radius: 10px;
                            margin: 20px 0;
                        }
                        .detail-row {
                            display: flex;
                            justify-content: space-between;
                            padding: 12px 0;
                            border-bottom: 1px solid #e2e8f0;
                        }
                        .detail-row:last-child {
                            border-bottom: none;
                        }
                        .detail-label {
                            font-weight: 600;
                            color: #666;
                        }
                        .detail-value {
                            color: #333;
                            font-weight: 600;
                        }
                        .solutions-box {
                            background-color: #e6f7ff;
                            padding: 20px;
                            border-radius: 10px;
                            margin: 25px 0;
                        }
                        .solution-item {
                            padding: 12px 0;
                            padding-left: 30px;
                            position: relative;
                        }
                        .solution-item:before {
                            content: "→";
                            position: absolute;
                            left: 0;
                            color: #1890ff;
                            font-weight: 700;
                            font-size: 18px;
                        }
                        .retry-box {
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            color: white;
                            padding: 25px;
                            border-radius: 15px;
                            margin: 25px 0;
                            text-align: center;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        }
                        .retry-box h3 {
                            margin: 0 0 10px 0;
                        }
                        .footer {
                            background-color: #2d3748;
                            color: #a0aec0;
                            padding: 30px;
                            text-align: center;
                            font-size: 14px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>⚠️ Pago No Procesado</h1>
                        </div>

                        <div class="content">
                            <div class="error-icon">😔</div>

                            <h2 style="color: #eb3349; text-align: center;">Hola %s</h2>

                            <p style="font-size: 16px; text-align: center;">
                                Lamentablemente, tu pago para el evento <strong>"%s"</strong> no pudo ser procesado en este momento.
                            </p>

                            <div class="error-box">
                                <strong>❌ No te preocupes</strong> - Este es un problema común y generalmente tiene solución rápida. A continuación te explicamos qué puedes hacer.
                            </div>

                            <div class="transaction-details">
                                <h3 style="color: #eb3349; margin-top: 0;">📋 Detalles de la Transacción</h3>
                                <div class="detail-row">
                                    <div class="detail-label">N° Transacción:</div>
                                    <div class="detail-value" style="font-family: monospace;">%s</div>
                                </div>
                                <div class="detail-row">
                                    <div class="detail-label">Motivo:</div>
                                    <div class="detail-value">%s</div>
                                </div>
                            </div>

                            <div class="solutions-box">
                                <h3 style="color: #1890ff; margin-top: 0;">💡 ¿Qué puedes hacer?</h3>
                                <div class="solution-item">
                                    <strong>Verifica tu saldo:</strong> Asegúrate de que tu tarjeta o cuenta tenga fondos suficientes
                                </div>
                                <div class="solution-item">
                                    <strong>Revisa los límites:</strong> Confirma que tu tarjeta no tenga restricciones de monto o compras en línea
                                </div>
                                <div class="solution-item">
                                    <strong>Prueba otro método:</strong> Intenta con una tarjeta diferente o método de pago alternativo
                                </div>
                                <div class="solution-item">
                                    <strong>Contacta a tu banco:</strong> Si el problema persiste, tu entidad financiera puede darte más información
                                </div>
                            </div>

                            <div class="retry-box">
                                <h3>🔄 Listo para intentar nuevamente</h3>
                                <p style="margin: 10px 0; opacity: 0.9;">
                                    Puedes reintentar el pago ingresando a tu perfil en la sección <strong>"Mis Inscripciones"</strong>
                                </p>
                            </div>

                            <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 5px;">
                                <p style="margin: 0;"><strong>⏰ Recuerda:</strong> Los cupos son limitados. Te recomendamos resolver el problema lo antes posible para asegurar tu participación en el evento.</p>
                            </div>

                            <div style="background-color: #f1f5f9; padding: 20px; border-radius: 10px; margin: 25px 0; text-align: center;">
                                <p style="margin: 0; color: #666;">
                                    <strong>¿Necesitas ayuda?</strong>
                                </p>
                                <p style="margin: 10px 0 0 0; color: #666;">
                                    Contáctanos a <strong>soporte@campolibre.com</strong> y estaremos encantados de asistirte
                                </p>
                            </div>
                        </div>

                        <div class="footer">
                            <p><strong>Campolibre</strong></p>
                            <p>Estamos aquí para ayudarte</p>
                            <p style="margin-top: 15px; font-size: 12px;">
                                © 2024 Campolibre. Todos los derechos reservados.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """,
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
            helper.setText(cuerpo, true); // true = HTML

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