package org.composer.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

@EnableRabbit
@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.queue}")
    private String queueName;

    @Value("${rabbitmq.nest-queue}")
    private String nestQueueName;

    @Value("${rabbitmq.python-queue}")
    private String pythonQueueName;
    @Value("${rabbitmq.exchange}")
    private String exchange;
    @Value("${rabbitmq.routingkey}")
    private String routingkey;

    @Value("${rabbitmq.nest-routingkey}")
    private String nestRoutingkey;

    @Value("${rabbitmq.python-routingkey}")
    private String pythonRoutingkey;
    @Value("${rabbitmq.username}")
    private String username;
    @Value("${rabbitmq.password}")
    private String password;
    @Value("${rabbitmq.host}")
    private String host;
    @Value("${rabbitmq.virtualhost}")
    private String virtualHost;
    @Value("${rabbitmq.reply.timeout}")
    private Integer replyTimeout;
    @Value("${rabbitmq.concurrent.consumers}")
    private Integer concurrentConsumers;
    @Value("${rabbitmq.max.concurrent.consumers}")
    private Integer maxConcurrentConsumers;
    @Bean
    public Queue queue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue pythonQueue() {
        return new Queue(pythonQueueName, true);
    }

    @Bean
    public Queue nestQueue() {
        return new Queue(nestQueueName, true);
    }
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchange);
    }
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingkey);
    }
    @Bean
    public Binding nestBinding() {
        return BindingBuilder.bind(nestQueue()).to(exchange()).with(nestRoutingkey);
    }

    @Bean
    public Binding pythonBinding() {
        return BindingBuilder.bind(pythonQueue()).to(exchange()).with(pythonRoutingkey);
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        return new Jackson2JsonMessageConverter(objectMapper);
    }
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setHost(host);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }
    @Bean
    public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setReplyTimeout(replyTimeout);
        rabbitTemplate.setUseDirectReplyToContainer(false);
        return rabbitTemplate;
    }
    @Bean
    public AsyncRabbitTemplate asyncRabbitTemplate(AmqpTemplate rabbitTemplate,ConnectionFactory connectionFactory) {
        final AsyncRabbitTemplate asyncRabbitTemplate = new AsyncRabbitTemplate((RabbitTemplate) rabbitTemplate);
        asyncRabbitTemplate.start();
        return asyncRabbitTemplate;
    }
    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(concurrentConsumers);
        factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
        factory.setErrorHandler(errorHandler());
        return factory;
    }
    @Bean
    public ErrorHandler errorHandler() {
        return new ConditionalRejectingErrorHandler(new MyFatalExceptionStrategy());
    }
    public static class MyFatalExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {
        private final Logger logger = LogManager.getLogger(getClass());
        @Override
        public boolean isFatal(Throwable t) {
            if (t instanceof ListenerExecutionFailedException) {
                ListenerExecutionFailedException lefe = (ListenerExecutionFailedException) t;
                logger.error("Failed to process inbound message from queue "
                        + lefe.getFailedMessage().getMessageProperties().getConsumerQueue()
                        + "; failed message: " + lefe.getFailedMessage(), t);
            }
            return super.isFatal(t);
        }
    }
}
