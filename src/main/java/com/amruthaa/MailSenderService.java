package com.amruthaa;

import com.amruthaa.models.SimpleMail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
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
        if(new File(mail.getContent()).isFile())
        {
            context.setVariable("header","Thanks again for choosing Machine Learning based SOA. Your model run has completed successfully and the results are ready to be viewed.");
            context.setVariable("footer","You can find it attached with this email.");
            FileSystemResource file = new FileSystemResource(new File(mail.getContent()));
            helper.addAttachment("result.png", file);
        }
        else
        {
            context.setVariable("header","Thanks again for choosing ML based SOA. Your model run has completed successfully. You can find the results below:");
            context.setVariable("content_text",mail.getContent());
            context.setVariable("footer","You can also download the result which is attached with this email.");
            InputStreamSource source =new ByteArrayResource(mail.getAttachment().getBytes(StandardCharsets.UTF_8));
            helper.addAttachment("result.json", source);
        }
        String html = templateEngine.process("emailResult", context);
        helper.setText(html,true);
        helper.setTo(mail.getTo());
        helper.setFrom(mail.getTo());
        mailSender.send(message);
    }
}
