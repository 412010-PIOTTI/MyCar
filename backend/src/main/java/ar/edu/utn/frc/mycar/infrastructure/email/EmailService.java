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
            log.warn("SendGrid API key not configured — 2FA code for {} is {}", toEmail, code);
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
