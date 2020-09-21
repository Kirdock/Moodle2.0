package com.aau.moodle20.services;

import com.aau.moodle20.entity.Course;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;


@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailServiceUnitTest{
    @InjectMocks
    private EmailService emailService;
    @Mock
    private JavaMailSender emailSender;

    @Test
    public void sendEmail_to_null()  {
        emailService.sendEmail(null,"test","text");
    }

    @Test
    public void sendEmail()  {
        doNothing().when(emailSender).send(any(SimpleMailMessage.class));
        emailService.sendEmail("ddd","test","text");
    }
}
