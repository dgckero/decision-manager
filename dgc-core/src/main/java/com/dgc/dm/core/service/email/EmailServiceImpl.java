/*
  @author david
 */

package com.dgc.dm.core.service.email;

import com.dgc.dm.core.dto.ProjectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private static final int NO_OF_QUICK_SERVICE_THREADS = 20;
    /**
     * this statement create a thread pool of twenty threads
     * here we are assigning send mail task using ScheduledExecutorService.submit();
     */
    private final ScheduledExecutorService quickService = Executors.newScheduledThreadPool(NO_OF_QUICK_SERVICE_THREADS);

    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * Return default email body formatted in HTML
     *
     * @param from
     * @return default email body
     */
    private static String generateDefaultEmailBodyHtml(final String from) {
        log.debug("[INIT] generateDefaultEmailBodyHtml from: {} ", from);

        final String htmlMessage = "\n" +
                "<html>\n" +
                "<body>\n" +
                "<h4>Se han encontrado sus datos de contacto en la aplicación decision-manager, por favor póngase en contacto con el administrador de la aplicación:</h4>" +
                "    <h5>Contacto del emisor: <a title=\"" + from + "\" href=\"mailto:" + from + "\">" + from + "</a>\n" +
                "</body>\n" +
                "</html>";

        log.debug("[END] Mensaje HTML a enviar {}", htmlMessage);

        return htmlMessage;
    }

    /**
     * Generate email body using parameters from, body, name
     *
     * @param from
     * @param body
     * @param name
     * @return email formatted on HTML
     */
    private static String generateEmailBodyHtml(final String from, final String body, final String name) {
        log.debug("[INIT] generateEmailBodyHtml from: {}, body: {}, name: {}", from, body, name);

        final String htmlMessage = "\n" +
                "<html>\n" +
                "<body>\n" +
                "<h4>Se ha recibido un mensaje en la aplicación dgc-web con los siguientes datos:</h4>" +
                "    <h5>Nombre del emisor: " + name + "</h5>\n" +
                "    <h5>Contacto del emisor: <a title=\"" + from + "\" href=\"mailto:" + from + "\">" + from + "</a>\n" +
                "  \t<h5>Mensaje:</h5>\n" +
                "    <h7>" + body + "</h7>  " +
                "</body>\n" +
                "</html>";

        log.debug("[END] Mensaje HTML a enviar {}", htmlMessage);
        return htmlMessage;
    }

    /**
     * Send email on Asynchronous mode
     *
     * @param toEmail
     * @param project
     */
    @Override
    public final void sendAsynchronousMail(final String toEmail, final ProjectDto project) {
        log.debug("[INIT] sendAsynchronousMail to {}", toEmail);

        final SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("prueba@gmail.com");
        mail.setTo(toEmail);
        mail.setSubject("test email");
        mail.setText(project.getEmailTemplate());

        this.quickService.submit(() -> {
            try {
                this.javaMailSender.send(mail);
            } catch (MailException e) {
                log.error("Exception occur while send a mail : ", e);
                e.printStackTrace();
            }
        });
        log.debug("[END] sendAsynchronousMail");
    }

    /**
     * Send email on synchronous mode
     *
     * @param to
     * @param project
     * @throws MessagingException
     */
    @Override
    public final void sendMail(final String to, final ProjectDto project) throws MessagingException {
        log.debug("[init] sendMail to: {}, project: {}", to, project);
        final MimeMessage message = this.generateEmailMessage(to, project);
        log.debug("Enviando mensaje {}", message);
        this.javaMailSender.send(message);
        log.debug("[END] Mensaje enviado correctamente");
    }

    /**
     * Send email on synchronous mode using parameters
     *
     * @param from
     * @param to
     * @param subject
     * @param body
     * @param name
     * @throws MessagingException
     */
    @Override
    public final void sendMail(final String from, final String to, final String subject, final String body, final String name) throws MessagingException {
        log.debug("[INIT] sendMail from: {}, to: {}, subject: {}, body: {}, name: {}", from, to, subject, body, name);
        final MimeMessage message = this.generateEmailMessage(from, to, subject, body, name);
        log.debug("Enviando mensaje {}", message);
        this.javaMailSender.send(message);
        log.debug("[END] Mensaje enviado correctamente");
    }

    /**
     * Create MimeMessage
     *
     * @param to
     * @param project
     * @return MimeMessage
     * @throws MessagingException
     */
    private MimeMessage generateEmailMessage(final String to, final ProjectDto project) throws MessagingException {
        log.debug("[INIT] generateEmailMessage to:  {}, Project: {}", to, project);

        final MimeMessage mail = this.javaMailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(mail, true);
        helper.setFrom("dgctrips@gmail.com");
        helper.setTo(to);
        helper.setSubject("test email");
        helper.setText(project.getEmailTemplate());

        log.debug("[END] generateEmailMessage mail: {}", mail);
        return mail;
    }

    /**
     * Create MimeMessage based on parameters
     *
     * @param from
     * @param to
     * @param subject
     * @param body
     * @param name
     * @return MimeMessage
     * @throws MessagingException
     */
    private MimeMessage generateEmailMessage(final String from, final String to, final String subject, final String body, final String name) throws MessagingException {
        log.debug("[INIT] generateEmailMessage from: {}, to:  {}, subject: {}, body: {}, name: {}", from, to, subject, body, name);

        final MimeMessage mail = this.javaMailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(mail, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(generateEmailBodyHtml(from, body, name), true);

        log.debug("[END] generateEmailMessage mail: {}", mail);
        return mail;
    }

}