package org.composer.core.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.composer.core.model.ModelUser;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.utils.Task;
import org.example.common.utils.AmqpMessageCustom;
import lombok.Builder;
import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Builder
public class AMQPAsyncProcessor implements AsyncProcessor {

    private AsyncRabbitTemplate asyncTemplate;

    private String exchangeName;
    private String nestRoutingkey;
   // private Message message;

    public AMQPAsyncProcessor( AsyncRabbitTemplate asyncTemplate, String exchangeName,
                        String nestRoutingkey){

        this.asyncTemplate = asyncTemplate;
        this.exchangeName = exchangeName;
        this.nestRoutingkey = nestRoutingkey;


    }
    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        CompletableFuture<Exchange> future = this.processAsync(exchange);
        future.whenComplete((res,err)->{
            callback.done(false);
        });

        return false;
    }

    @Override
    public CompletableFuture<Exchange> processAsync(Exchange exchange) {
       ObjectMapper objectMapper = new ObjectMapper();
        CompletableFuture<Message> future = this.asyncTemplate.sendAndReceive(this.exchangeName,
                this.nestRoutingkey, AmqpMessageCustom.getNestMessage("",
                        "{\"query\":\"find-all-users\"}",
                        exchange.getExchangeId()));

        CompletableFuture<Exchange> exchangeFuture = future.thenApply(message->{
            CompareUsersModel body =  exchange.getMessage().getBody( CompareUsersModel.class);
            try {
                ModelUser[] amqpResult = objectMapper.readValue(message.getBody(),ModelUser[].class);

                List<ModelUser> list = Stream.of(amqpResult).collect(Collectors.toList());
                var  currentTask = (Task<String, String, List<ModelUser>>)body.getCurrentTask();
                currentTask.setOutput(list);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            exchange.getIn().setBody(body);
            return exchange;
        }).exceptionally(err-> {
                    exchange.setException(err);
                    return exchange;
                }
        );
        return exchangeFuture;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

    }
}
