package com.amruthaa.controller;

import com.amruthaa.MessageQueue.MessageSender;
import com.amruthaa.configuration.ApplicationConfigReader;
import com.amruthaa.models.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
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
    public String index() { return "upload"; }

    @PostMapping("/upload") // //new annotation since 4.3
    public String singleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:uploadStatus";
        }

        try {

            // Get the file and save it in upload folder
            byte[] bytes = file.getBytes();
            Path path = Paths.get(applicationConfig.getUploadFolder() + file.getOriginalFilename());
            Files.write(path, bytes);

            /* Sending to Message Queue */
            String exchange = applicationConfig.getApp1Exchange();
            String routingKey = applicationConfig.getApp1RoutingKey();
            UserInput inputMessage=new UserInput("1","543543653","Random Forest","https://aws.amazon.com/435324232r453fdd");
            messageSender.sendMessage(rabbitTemplate, exchange, routingKey, inputMessage);

            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception ex) {
            log.error("Exception occurred while sending message to the queue. Exception= {}", ex);
        }

        return "redirect:/uploadStatus";
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }

}