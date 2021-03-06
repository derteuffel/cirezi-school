package com.derteuffel.school.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Created by derteuffel on 31/10/2018.
 */
@Component
public class MailService {
    @Autowired
    private  JavaMailSender mailSender;
    public MailService()
    {
        mailSender = getJavaMailSender();
    }
    static public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("metoupeguy@gmail.com");
        mailSender.setPassword("breakofdawn1994");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

      //  props.put("mail.smtp.socketFactory.port", "465");
      //  props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
       // props.put("mail.smtp.socketFactory.fallback", "false");

        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
       public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        }



}
