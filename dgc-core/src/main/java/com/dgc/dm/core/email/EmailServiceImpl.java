/*
  @author david
 */

package com.dgc.dm.core.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    JavaMailSender mailSender;

    @Override
    public void sendMail(String to) throws MessagingException {
        final MimeMessage message = this.generateEmailMessage(to);

        log.info("Enviando mensaje " + message);
        this.mailSender.send(message);
        log.info("Mensaje enviado correctamente");
    }

    @Override
    public void sendMail(final String from, final String to, final String subject, final String body, final String name) throws MessagingException {
        final MimeMessage message = this.generateEmailMessage(from, to, subject, body, name);

        log.info("Enviando mensaje " + message);
        this.mailSender.send(message);
        log.info("Mensaje enviado correctamente");
    }

    private MimeMessage generateEmailMessage(final String from, final String to, final String subject, final String body, final String name) throws MessagingException {

        final MimeMessage mail = this.mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(mail, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(this.generateEmailBodyHtml(from, body, name), true);

        return mail;
    }

    private MimeMessage generateEmailMessage(final String to) throws MessagingException {

        final MimeMessage mail = this.mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(mail, true);
        helper.setFrom("dgctrips@gmail.com");
        helper.setTo(to);
        helper.setSubject("test email");
        helper.setText(this.generateDefaultEmailBodyHtml("dgctrips@gmail.com"), true);

        return mail;
    }

    private String generateDefaultEmailBodyHtml(final String from) {

        final String htmlMessage = "\n" +
                "<html>\n" +
                "<body>\n" +
                "<h4>Se han encontrado sus datos de contacto en la aplicación decision-manager, por favor póngase en contacto con el administrador de la aplicación:</h4>" +
                "    <h5>Contacto del emisor: <a title=\"" + from + "\" href=\"mailto:" + from + "\">" + from + "</a>\n" +
                "</body>\n" +
                "</html>";

        log.debug("Mensaje HTML a enviar " + htmlMessage);

        return htmlMessage;
    }

    private String generateEmailBodyHtml(final String from, final String body, final String name) {

        final String htmlMessage = "\n" +
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
