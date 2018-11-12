package com.amruthaa.MessageQueue;

import com.amruthaa.ApplicationConstant;
import com.amruthaa.MailSenderService;
import com.amruthaa.MlApiRunner;
import com.amruthaa.configuration.ApplicationConfigReader;
import com.amruthaa.models.SimpleMail;
import com.amruthaa.models.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

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
    private MailSenderService senderService;


    /**
     * Message listener for input queue
     * @param data a user defined object used for deserialization of message
     */
    @RabbitListener(queues = "${inputqueue.queue.name}")
    public void receiveMessageFromInputQueue(final UserInput data) {
        log.info("Received message: {} from Input queue.", data);
        try {
            log.info("Making REST call to the respective Machine learning API(based on the model)");
            String apiOut="";
            switch(data.getModel())
            {
                case "SentimentalAnalysis":
                    apiOut = MlApiRunner.sentimentalAnalysisApi(applicationConfigReader.getApi1Url(),data);
                    break;
                case "ImageClassification":
                    apiOut = MlApiRunner.imageClassificationApi(applicationConfigReader.getApi2Url(),data);
                    break;

                default:
                    apiOut = "Either data is incorrect or api call feature collection is invalid";
                    break;
            }

            log.info("<< Exiting receiveMessageForApp1() after API call.");
            log.info("<< Output accuracy to output message queue...");

            data.setResult(apiOut);

            String exchange = applicationConfigReader.getOutputQueueExchange();
            String routingKey = applicationConfigReader.getOutputQueueKey();

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
    @RabbitListener(queues = "${outputqueue.queue.name}")
    public void receiveMessageFromOutputQueue(final UserInput data) {
        log.info("Received message: {} from Output queue.", data);
        try {
            log.info("Sending an email to user");
            // send a simple mail
            String subject = "Ticket Id#" + data.getTicketId() + "- ML based SOA run completed!";

            // content should have three keys. one of the key will be image base64 format. pass that to content which can then be displayed in image
            // https://www.thymeleaf.org/doc/articles/springmail.html
            String content = data.getResult();
            senderService.sendSimpleMail(new SimpleMail(data.getEmail(),subject,content,data.getResult()));

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
