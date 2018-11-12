package com.amruthaa;

import com.amruthaa.models.SimpleMail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Service
public class MailSenderService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendSimpleMail(SimpleMail mail) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setSubject(mail.getSubject());
        Context context = new Context();
        context.setVariable("header","Thanks again for choosing ML based SOA. Your model run has completed successfully. You can find the results below:");
        context.setVariable("content",mail.getContent());
        context.setVariable("footer","You can also download the result which is attached with this email.");
        String html = templateEngine.process("emailResult", context);
        helper.setText(html,true);
        helper.setTo(mail.getTo());
        helper.setFrom(mail.getTo());
        InputStreamSource source =new ByteArrayResource(mail.getAttachment().getBytes(StandardCharsets.UTF_8));
        helper.addAttachment("result.json", source);
        mailSender.send(message);
    }
}
