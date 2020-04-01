/*
  @author david
 */

package com.dgc.dm.core.email;

import javax.mail.MessagingException;

public interface EmailService {
    void sendMail(String from, String to, String subject, String body, String name) throws MessagingException;

    void sendMail(String to) throws MessagingException;
}
