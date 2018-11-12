package com.amruthaa;


import com.amruthaa.configuration.ApplicationConfigReader;
import lombok.Data;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

@EnableRabbit
@SpringBootApplication
@Data
public class Application extends SpringBootServletInitializer implements RabbitListenerConfigurer {

    @Autowired
    private ApplicationConfigReader applicationConfig;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    /* This bean is to read the properties file configs */
    @Bean
    public ApplicationConfigReader applicationConfig() {
        return new ApplicationConfigReader();
    }


    /* Creating a bean for the Input Message queue Exchange */
    @Bean
    public TopicExchange getInputQueueExchange() {
        return new TopicExchange(getApplicationConfig().getInputQueueExchange());
    }

    /* Creating a bean for the Input Message queue */
    @Bean
    public Queue getInputQueueName() {
        return new Queue(getApplicationConfig().getInputQueueName());
    }

    /* Binding between Input Exchange and Queue using routing key */
    @Bean
    public Binding declareBindingInputQueue() {
        return BindingBuilder.bind(getInputQueueName()).to(getInputQueueExchange()).with(getApplicationConfig().getInputQueueKey());
    }

    /* Creating a bean for the Output Message queue Exchange */
    @Bean
    public TopicExchange getOutputQueueExchange() {
        return new TopicExchange(getApplicationConfig().getOutputQueueExchange());
    }

    /* Creating a bean for the Message queue */
    @Bean
    public Queue getOutputQueueName() {
        return new Queue(getApplicationConfig().getOutputQueueName());
    }

    /* Binding between Output Exchange and Queue using routing key */
    @Bean
    public Binding declareBindingOutputQueue() {
        return BindingBuilder.bind(getOutputQueueName()).to(getOutputQueueExchange()).with(getApplicationConfig().getOutputQueueKey());
    }

    /* Bean for rabbitTemplate */
    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public MappingJackson2MessageConverter consumerJackson2MessageConverter() {
        return new MappingJackson2MessageConverter();
    }

    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(consumerJackson2MessageConverter());
        return factory;
    }

    //Tomcat large file upload connection reset
    @Bean
    public TomcatServletWebServerFactory tomcatEmbedded() {

        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();

        tomcat.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> {
            if ((connector.getProtocolHandler() instanceof AbstractHttp11Protocol<?>)) {
                int maxUploadSizeInMb = getApplicationConfig().getUploadFileMaxSize();
                //-1 means unlimited
                ((AbstractHttp11Protocol<?>) connector.getProtocolHandler()).setMaxSwallowSize(maxUploadSizeInMb);
            }
        });

        return tomcat;

    }

    @Override
    public void configureRabbitListeners(final RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }
}
