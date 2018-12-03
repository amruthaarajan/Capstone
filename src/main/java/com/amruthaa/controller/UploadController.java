package com.amruthaa.controller;

import com.amruthaa.MessageQueue.MessageSender;
import com.amruthaa.TicketIdGenerator;
import com.amruthaa.configuration.ApplicationConfigReader;
import com.amruthaa.models.UserInput;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    private final RabbitTemplate rabbitTemplate;
    private ApplicationConfigReader applicationConfig;
    private MessageSender messageSender;

    @Autowired
    public void setApplicationConfig(ApplicationConfigReader applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Autowired
    public UploadController(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Autowired
    public void setMessageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @RequestMapping("/")
    public String index() { return "intro"; }

    @RequestMapping("/intro")
    public String intro(@RequestParam(value = "terms", defaultValue = "off") String terms) {
        if (terms.equals("on")) {
            return "home";
        }
        return "intro";
    }

    @PostMapping("/home") // //new annotation since 4.3
    public String singleFileUpload(@RequestParam("model") String model, @RequestParam("email") String email, @RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        return doAnalysis(model,email,file,redirectAttributes);
    }

    @GetMapping("/confirmation")
    public String confirmation() {
        return "confirmation";
    }

    private String doAnalysis(String model, String email,MultipartFile file, RedirectAttributes redirectAttributes)
    {
        try {

            int ticketId = TicketIdGenerator.generate();
            // Get the file and save it in upload folder
            byte[] bytes = file.getBytes();
            Path path = Paths.get(applicationConfig.getUploadFolder() + ticketId + "/" + file.getOriginalFilename());
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);

            /* Sending to Message Queue */
            String exchange = applicationConfig.getInputQueueExchange();
            String routingKey = applicationConfig.getInputQueueKey();
            UserInput inputMessage=new UserInput(email, ticketId,model,path.toString());
            messageSender.sendMessage(rabbitTemplate, exchange, routingKey, inputMessage);

            redirectAttributes.addFlashAttribute("message",
                    "Thanks for using the service. You successfully uploaded '" + file.getOriginalFilename() + "' and initiated a run. Your results will be sent to '" + email + "' when completed.");

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception ex) {
            log.error("Exception occurred while sending message to the queue. Exception= {}", ex);
        }

        return "redirect:/confirmation";
    }
}