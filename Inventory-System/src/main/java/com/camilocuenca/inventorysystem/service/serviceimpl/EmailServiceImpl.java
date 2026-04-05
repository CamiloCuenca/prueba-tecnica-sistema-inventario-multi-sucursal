package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.email.EmailDTO;
import com.camilocuenca.inventorysystem.service.serviceInterface.EmailService;
import jakarta.annotation.PostConstruct;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Value("${app.mail.username:}")
    private String smtpUsername;

    @Value("${app.mail.password:}")
    private String smtpPassword;

    @Value("${app.mail.host:smtp.gmail.com}")
    private String smtpHost;

    @Value("${app.mail.port:587}")
    private int smtpPort;

    @Value("${app.mail.transport-strategy:SMTP_TLS}")
    private String transportStrategyProp;

    private TransportStrategy transportStrategy;

    @PostConstruct
    public void init() {
        // Validar propiedades mínimas
        if (smtpUsername == null || smtpUsername.isBlank() || smtpPassword == null || smtpPassword.isBlank()) {
            log.warn("Credenciales SMTP no configuradas. EmailService no enviará correos hasta que se configuren las propiedades app.mail.*");
        }
        try {
            transportStrategy = TransportStrategy.valueOf(transportStrategyProp);
        } catch (Exception e) {
            transportStrategy = TransportStrategy.SMTP_TLS;
        }
    }

    @Override
    @Async
    public void sendMail(EmailDTO emailDTO) throws Exception {
        Objects.requireNonNull(emailDTO, "emailDTO no puede ser nulo");

        if (smtpUsername == null || smtpUsername.isBlank() || smtpPassword == null || smtpPassword.isBlank()) {
            log.error("No se enviará el email porque las credenciales SMTP no están configuradas.");
            return;
        }

        log.info("Enviando correo a {} subject={}", emailDTO.recipient(), emailDTO.issue());

        Email email = EmailBuilder.startingBlank()
                .from(smtpUsername)
                .to(emailDTO.recipient())
                .withSubject(emailDTO.issue())
                .withHTMLText(emailDTO.body())
                .buildEmail();

        try (Mailer mailer = MailerBuilder
                .withSMTPServer(smtpHost, smtpPort, smtpUsername, smtpPassword)
                .withTransportStrategy(transportStrategy)
                .withDebugLogging(true)
                .buildMailer()) {
            mailer.sendMail(email);
            log.info("Correo enviado a {}", emailDTO.recipient());
        } catch (Exception e) {
            log.error("Error enviando correo a {}: {}", emailDTO.recipient(), e.getMessage(), e);
            throw e;
        }
    }
}

