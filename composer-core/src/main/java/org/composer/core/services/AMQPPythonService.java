package org.composer.core.services;

import lombok.Data;
import org.apache.camel.Exchange;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@Data
public class AMQPPythonService {

    @Value("${rabbitmq.python-queue}")
    private String pythonQueueName;

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.python-routingkey}")
    private String pythonRoutingkey;

    @Autowired
    private AmqpTemplate rabbitTemplate;
    public void processPythonAmqp(Exchange exchange){

                Message pythonResult = (Message) rabbitTemplate
                .sendAndReceive(exchangeName,
                        pythonRoutingkey,
                        new Message(new String("{\"name\":\"John\"}").getBytes(StandardCharsets.UTF_8))
                        );

    }
}
