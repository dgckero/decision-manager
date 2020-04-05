/*
  @author david
 */

package com.dgc.dm.core.email;

import com.dgc.dm.core.dto.ProjectDto;
import org.springframework.mail.MailException;

import javax.mail.MessagingException;

public interface EmailService {
    void sendASynchronousMail(String toEmail, ProjectDto project) throws MailException;

    void sendMail(String from, String to, String subject, String body, String name) throws MessagingException;

    void sendMail(String to, ProjectDto project) throws MessagingException;
}
