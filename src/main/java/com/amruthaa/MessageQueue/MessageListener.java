package com.amruthaa.MessageQueue;

import com.amruthaa.ApplicationConstant;
import com.amruthaa.configuration.ApplicationConfigReader;
import com.amruthaa.models.Result;
import com.amruthaa.models.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.mail.internet.MimeMessage;

/**
 * Message Listener for RabbitMQ
 */
@Service
public class MessageListener {
    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    @Autowired
    private MessageSender messageSender;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    ApplicationConfigReader applicationConfigReader;

    @Autowired
    private JavaMailSender emailSender;


    /**
     * Message listener for input queue
     * @param UserInput a user defined object used for deserialization of message
     */
    @RabbitListener(queues = "${app1.queue.name}")
    public void receiveMessageFromInputQueue(final UserInput data) {
        log.info("Received message: {} from Input queue.", data);
        try {
            log.info("Making REST call to the respective Machine learning API(based on the model)");
            //TODO: Code to make REST call to machine learning API

            log.info("<< Exiting receiveMessageForApp1() after API call.");
            log.info("<< Output accuracy to output message queue...");

            // TODO: publish to output message queue
            String exchange = applicationConfigReader.getApp2Exchange();
            String routingKey = applicationConfigReader.getApp2RoutingKey();

            messageSender.sendMessage(rabbitTemplate, exchange, routingKey, data);

        } catch(HttpClientErrorException  ex) {
            if(ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("Delay...");
                try {
                    Thread.sleep(ApplicationConstant.MESSAGE_RETRY_DELAY);
                } catch (InterruptedException e) { }
                log.info("Throwing exception so that message will be requed in the queue.");
                // Note: Typically Application specific exception should be thrown below
                throw new RuntimeException();
            } else {
                throw new AmqpRejectAndDontRequeueException(ex);
            }
        } catch(Exception e) {
            log.error("Internal server error occurred in API call. Bypassing message requeue {}", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    /**
     * Message listener for Output queue
     *
     */
    @RabbitListener(queues = "${app2.queue.name}")
    public void receiveMessageFromOutputQueue(final UserInput data) {
        log.info("Received message: {} from Output queue.", data);
        try {
            log.info("Sending an email to user");
            //TODO: Send a email to user
            MimeMessage emailMessage = emailSender.createMimeMessage();
            MimeMessageHelper emailHelper = new MimeMessageHelper(emailMessage);
            emailHelper.setTo(data.getEmail());
            Result outputEmailText=new Result(data.getModel(),"89.5","23minutes");
            emailHelper.setText("Here are the results: " + outputEmailText);
            emailSender.send(emailMessage);

            log.info("<< Exiting receiveMessageCrawlCI() after sending an email.");
        } catch(HttpClientErrorException  ex) {
            if(ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("Delay...");
                try {
                    Thread.sleep(ApplicationConstant.MESSAGE_RETRY_DELAY);
                } catch (InterruptedException e) { }
                log.info("Throwing exception so that message will be requed in the queue.");
                // Note: Typically Application specific exception can be thrown below
                throw new RuntimeException();
            } else {
                throw new AmqpRejectAndDontRequeueException(ex);
            }
        } catch(Exception e) {
            log.error("Internal server error occurred in server. Bypassing message requeue {}", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
