package com.amruthaa;

import com.amruthaa.models.SimpleMail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Service
public class MailSenderService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleMail(SimpleMail mail) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setSubject(mail.getSubject());
        helper.setText(mail.getContent());
        helper.setTo(mail.getTo());
        helper.setFrom(mail.getTo());
        InputStreamSource source =new ByteArrayResource(mail.getAttachment().getBytes(StandardCharsets.UTF_8));
        helper.addAttachment("result.txt", source);
        mailSender.send(message);
    }
}
