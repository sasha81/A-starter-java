package org.composer.core.services;


import org.example.common.utils.AmqpMessageCustom;
import lombok.Data;
import org.apache.camel.Exchange;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Data
@Service
public class AMQPNestService {

    @Value("${rabbitmq.exchange}")
    private String exchangeName;
    @Value("${rabbitmq.nest-queue}")
    private String nestQueueName;

    @Value("${rabbitmq.nest-routingkey}")
    private String nestRoutingkey;

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void processNestAmqp(Exchange exchange){

        Message nestResult = (Message) rabbitTemplate
                .sendAndReceive(exchangeName,
                        nestRoutingkey,
                        AmqpMessageCustom.getNestMessage("Sasha","{\"cmd\":\"greet\"}",exchange.getExchangeId())
                );

    }
}
