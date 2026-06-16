package ar.edu.utn.frc.mycar.infrastructure.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {

    @Value("${app.sendgrid.api-key:}")
    private String apiKey;

    @Value("${app.sendgrid.from-email:noreply@mycar.app}")
    private String fromEmail;

    public void send2FACode(String toEmail, String code) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("SendGrid API key not configured — skipping email to {}", toEmail);
            return;
        }

        Email from = new Email(fromEmail, "MyCar");
        Email to = new Email(toEmail);
        String subject = "Tu código de verificación MyCar";
        Content content = new Content("text/html", buildEmailBody(code));
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException e) {
            log.error("Failed to send 2FA email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error al enviar el email de verificación", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("SendGrid API key not configured — skipping password reset email to {}", toEmail);
            return;
        }

        Email from = new Email(fromEmail, "MyCar");
        Email to = new Email(toEmail);
        String subject = "Recuperá tu contraseña MyCar";
        Content content = new Content("text/html", buildResetEmailBody(resetLink));
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error al enviar el email de recuperación de contraseña", e);
        }
    }

    private String buildResetEmailBody(String resetLink) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 480px; margin: 0 auto; background: #fff; border-radius: 8px;
                              padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,.08);">
                    <h1 style="color: #2563eb; font-size: 22px; margin-bottom: 8px;">MyCar</h1>
                    <h2 style="color: #1e293b; font-size: 18px; margin-bottom: 16px;">
                      Recuperá tu contraseña
                    </h2>
                    <p style="color: #475569; font-size: 14px; margin-bottom: 24px;">
                      Recibimos una solicitud para restablecer la contraseña de tu cuenta.
                      Hacé clic en el botón de abajo para continuar.
                      El enlace es válido por <strong>1 hora</strong>.
                    </p>
                    <div style="text-align: center; margin: 32px 0;">
                      <a href="%s"
                         style="display: inline-block; background: #2563eb; color: #fff;
                                font-size: 15px; font-weight: 600; text-decoration: none;
                                padding: 14px 32px; border-radius: 8px;">
                        Restablecer contraseña
                      </a>
                    </div>
                    <p style="color: #475569; font-size: 13px; margin-bottom: 8px;">
                      O copiá este enlace en tu navegador:
                    </p>
                    <p style="color: #2563eb; font-size: 12px; word-break: break-all;">%s</p>
                    <p style="color: #94a3b8; font-size: 12px; margin-top: 24px;">
                      Si no solicitaste este cambio, podés ignorar este email.
                      Tu contraseña permanece sin cambios.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(resetLink, resetLink);
    }

    private String buildEmailBody(String code) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                  <div style="max-width: 480px; margin: 0 auto; background: #fff; border-radius: 8px;
                              padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,.08);">
                    <h1 style="color: #2563eb; font-size: 22px; margin-bottom: 8px;">MyCar</h1>
                    <h2 style="color: #1e293b; font-size: 18px; margin-bottom: 16px;">
                      Código de verificación
                    </h2>
                    <p style="color: #475569; font-size: 14px; margin-bottom: 24px;">
                      Usá el siguiente código para completar tu inicio de sesión.
                      Es válido por <strong>10 minutos</strong>.
                    </p>
                    <div style="text-align: center; margin: 32px 0;">
                      <span style="display: inline-block; background: #f1f5f9; border-radius: 8px;
                                   padding: 16px 32px; font-size: 36px; font-weight: 700;
                                   letter-spacing: 10px; color: #1e293b; font-family: monospace;">
                        %s
                      </span>
                    </div>
                    <p style="color: #94a3b8; font-size: 12px; margin-top: 24px;">
                      Si no solicitaste este código, podés ignorar este email.
                      Tu cuenta permanece segura.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(code);
    }
}
