/*
  @author david
 */

package com.dgc.dm.core.email;

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
    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * this statement create a thread pool of twenty threads
     * here we are assigning send mail task using ScheduledExecutorService.submit();
     */
    private final ScheduledExecutorService quickService = Executors.newScheduledThreadPool(NO_OF_QUICK_SERVICE_THREADS); // Creates a thread pool that reuses fixed number of threads(as specified by noOfThreads in this case).

    @Override
    public void sendASynchronousMail(String toEmail, ProjectDto project) throws MailException {
        log.debug("sendASynchronousMail to " + toEmail);
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("prueba@gmail.com");
        mail.setTo(toEmail);
        mail.setSubject("test email");
        mail.setText(project.getEmailTemplate());

        quickService.submit(() -> {
            try {
                javaMailSender.send(mail);
            } catch (final MailException e) {
                log.error("Exception occur while send a mail : ", e);
                e.printStackTrace();
            }
        });
    }

    @Override
    public final void sendMail(String to, ProjectDto project) throws MessagingException {
        MimeMessage message = generateEmailMessage(to, project);

        log.info("Enviando mensaje " + message);
        javaMailSender.send(message);
        log.info("Mensaje enviado correctamente");
    }

    @Override
    public void sendMail(String from, String to, String subject, String body, String name) throws MessagingException {
        MimeMessage message = generateEmailMessage(from, to, subject, body, name);

        log.info("Enviando mensaje " + message);
        javaMailSender.send(message);
        log.info("Mensaje enviado correctamente");
    }

    private MimeMessage generateEmailMessage(String from, String to, String subject, String body, String name) throws MessagingException {

        MimeMessage mail = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(generateEmailBodyHtml(from, body, name), true);

        return mail;
    }

    private MimeMessage generateEmailMessage(String to, ProjectDto project) throws MessagingException {

        MimeMessage mail = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail, true);
        helper.setFrom("dgctrips@gmail.com");
        helper.setTo(to);
        helper.setSubject("test email");
        helper.setText(project.getEmailTemplate());

        return mail;
    }

    private String generateDefaultEmailBodyHtml(String from) {

        String htmlMessage = "\n" +
                "<html>\n" +
                "<body>\n" +
                "<h4>Se han encontrado sus datos de contacto en la aplicación decision-manager, por favor póngase en contacto con el administrador de la aplicación:</h4>" +
                "    <h5>Contacto del emisor: <a title=\"" + from + "\" href=\"mailto:" + from + "\">" + from + "</a>\n" +
                "</body>\n" +
                "</html>";

        log.debug("Mensaje HTML a enviar " + htmlMessage);

        return htmlMessage;
    }

    private String generateEmailBodyHtml(String from, String body, String name) {

        String htmlMessage = "\n" +
                "<html>\n" +
                "<body>\n" +
                "<h4>Se ha recibido un mensaje en la aplicación DGCAcademy con los siguientes datos:</h4>" +
                "    <h5>Nombre del emisor: " + name + "</h5>\n" +
                "    <h5>Contacto del emisor: <a title=\"" + from + "\" href=\"mailto:" + from + "\">" + from + "</a>\n" +
                "  \t<h5>Mensaje:</h5>\n" +
                "    <h7>" + body + "</h7>  " +
                "</body>\n" +
                "</html>";

        log.debug("Mensaje HTML a enviar " + htmlMessage);

        return htmlMessage;
    }

}