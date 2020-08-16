package com.aau.moodle20.services;

import com.aau.moodle20.exception.ServiceException;
import com.sun.mail.smtp.SMTPTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
public class EmailService {

    private JavaMailSender emailSender;

    @Value("${senderEmailUsername}")
    private String from;

    public EmailService (JavaMailSender emailSender)
    {
        this.emailSender = emailSender;
    }

    public void sendEmail(String to, String subject, String text) {
        if (to == null)
            return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
