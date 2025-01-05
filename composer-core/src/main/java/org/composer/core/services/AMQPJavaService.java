package org.composer.core.services;

import org.apache.camel.Exchange;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class AMQPJavaService {
    @Value("${rabbitmq.queue}")
    private String queueName;

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routingkey}")
    private String routingkeyName;

    @Autowired
    private AmqpTemplate rabbitTemplate;


    public void processAmqpJava(Exchange exchange){
                Message result = (Message) rabbitTemplate
                .sendAndReceive(exchangeName,
                        routingkeyName,
                        new Message(new String("{\"id\":1,\"description\":\"John\"}").getBytes(StandardCharsets.UTF_8))
                        );
    }
}
