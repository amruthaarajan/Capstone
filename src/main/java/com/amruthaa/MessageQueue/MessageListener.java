package com.amruthaa.MessageQueue;

import com.amruthaa.ApplicationConstant;
import com.amruthaa.configuration.ApplicationConfigReader;
import com.amruthaa.models.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
    ApplicationConfigReader applicationConfigReader;
    /**
     * Message listener for input queue
     * @param UserDetails a user defined object used for deserialization of message
     */
    @RabbitListener(queues = "${app1.queue.name}")
    public void receiveMessageFromInputQueue(final UserDetails data) {
        log.info("Received message: {} from app1 queue.", data);
        try {
            log.info("Making REST call to the API");
            //TODO: Code to make REST call
            log.info("<< Exiting receiveMessageForApp1() after API call.");
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
    public void receiveMessageFromOutputQueue(String reqObj) {
        log.info("Received message: {} from app2 queue.", reqObj);
        try {
            log.info("Making REST call to the API");
            //TODO: Send a email to user
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
            log.error("Internal server error occurred in python server. Bypassing message requeue {}", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
