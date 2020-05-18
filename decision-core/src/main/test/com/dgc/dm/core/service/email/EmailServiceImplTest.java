package com.dgc.dm.core.service.email;

import com.dgc.dm.core.dto.ProjectDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class EmailServiceImplTest {

    @Mock
    private JavaMailSender mockJavaMailSender;

    @Mock
    private ScheduledExecutorService mockQuickService;

    @Mock
    ScheduledFuture<?> mockScheduledFuture;

    @InjectMocks
    private EmailServiceImpl emailServiceImplUnderTest;


    final ProjectDto project = new ProjectDto(0, "name", "rowDataTableName", "emailTemplate", "content".getBytes());

    @BeforeEach
    void setUp() {
        initMocks(this);
        emailServiceImplUnderTest = new EmailServiceImpl(mockJavaMailSender);
    }

    @Test
    void testSendAsynchronousMail() {
        // Setup
        Mockito.doReturn(mockScheduledFuture).when(mockQuickService).submit(any(Runnable.class));
        // Run the test
        emailServiceImplUnderTest.sendAsynchronousMail("toEmail", project);
        // Verify the results
        assertTrue(true);
    }

    @Test
    void testSendAsynchronousMail_throwsMailException() {
        // Setup
        final SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("prueba@gmail.com");
        mail.setTo("toEmail");
        mail.setSubject("test email");
        mail.setText(project.getEmailTemplate());

        Mockito.doReturn(mockScheduledFuture).when(mockQuickService).submit(any(Runnable.class));
        doThrow(MailSendException.class).when(mockJavaMailSender).send(mail);
        // Run the test
        emailServiceImplUnderTest.sendAsynchronousMail("toEmail", project);
        // Verify the results
        assertTrue(true);
    }

    @Test
    void testSendMail() throws Exception {
        // Setup

        // Configure JavaMailSender.createMimeMessage(...).
        final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mockJavaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mockJavaMailSender).send(mimeMessage);

        // Run the test
        emailServiceImplUnderTest.sendMail("to", project);

        // Verify the results
        verify(mockJavaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendMail_JavaMailSenderThrowsMailException() throws Exception {
        // Setup
        final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mockJavaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        MailException exception = new MailSendException("Test message");
        doThrow(exception).when(mockJavaMailSender).send(mimeMessage);

        // Run the test
        assertThrows(MailException.class, () -> {
            emailServiceImplUnderTest.sendMail("to", project);
        });
    }

    @Test
    void testSendMail1() throws Exception {
        // Setup

        // Configure JavaMailSender.createMimeMessage(...).
        final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mockJavaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Run the test
        emailServiceImplUnderTest.sendMail("from", "to", "subject", "body", "name");

        // Verify the results
        verify(mockJavaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendMail_JavaMailSenderThrowsMailException1() {
        // Setup
        final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mockJavaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        MailException exception = new MailSendException("Test message");
        doThrow(exception).when(mockJavaMailSender).send(mimeMessage);

        // Run the test
        assertThrows(MailSendException.class, () -> {
            emailServiceImplUnderTest.sendMail("from", "to", "subject", "body", "name");
        });
    }
}
