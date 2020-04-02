/*
  @author david
 */

package com.dgc.dm.core.email;

import com.dgc.dm.core.dto.ProjectDto;

import javax.mail.MessagingException;

public interface EmailService {
    void sendASynchronousMail(String toEmail, ProjectDto project) throws RuntimeException;

    void sendMail(String from, String to, String subject, String body, String name) throws MessagingException;

    void sendMail(String to, ProjectDto project) throws MessagingException;
}
