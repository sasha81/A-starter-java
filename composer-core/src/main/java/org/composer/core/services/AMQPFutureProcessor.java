package org.composer.core.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.composer.core.converters.AMQPModelUserDto;
import org.composer.core.converters.AMQPUserModelDtoContainer;
import org.composer.core.converters.GetUserModel;
import org.composer.core.model.ModelUser;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.utils.Task;
import org.example.common.utils.AmqpMessageCustom;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AsyncAmqpTemplate;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AMQPFutureProcessor extends AbstractFutureAsyncProcessor<String,String, Message>{
    Logger logger = LoggerFactory.getLogger(AMQPFutureProcessor.class);
    private AsyncAmqpTemplate asyncTemplate;
    private String rabbitExchangeName;
    private String rabbitRoutingKey;

    private ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);;
    public AMQPFutureProcessor(AsyncAmqpTemplate asyncTemplate, String rabbitExchangeName, String rabbitRoutingKey) {
        this.asyncTemplate = asyncTemplate;
        this.rabbitExchangeName = rabbitExchangeName;
        this.rabbitRoutingKey = rabbitRoutingKey;
    }

    @Override
    protected CompletableFuture<Message> getOutputFutureMethod(Exchange exchange, String input, String metadata) {
        CompletableFuture<Message> result = this.asyncTemplate.sendAndReceive(this.rabbitExchangeName,
                this.rabbitRoutingKey, AmqpMessageCustom.getNestMessage(input,
                        "{\"query\":\"find-all-users\"}",
                        metadata));
        return result;
    }

    @Override
    protected String getInputFromExchange(Exchange exchange) {

        CompareUsersModel body =  exchange.getMessage().getBody(CompareUsersModel.class);
        String arg = body.getCurrentTask().getInput();
        return arg;
    }

    @Override
    protected String getMetadataFromExchange(Exchange exchange) {
        return exchange.getExchangeId();
    }

    @Override
    protected Function<Message, Exchange> getExchangeFutureMethod(Exchange exchange)  {
        return message->{

              try {
                  AMQPUserModelDtoContainer amqpResult = this.objectMapper.readValue(message.getBody(), AMQPUserModelDtoContainer.class);
                  CompareUsersModel body = setBody(exchange, amqpResult.getResponse());
                  exchange.getMessage().setBody(body);
              } catch (IOException e) {
                  throw new RuntimeException(e);
              }

              return exchange;

        };
    }
    @Override
    protected Function<Throwable, Exchange> getErrorHandler(Exchange exchange){
        return err->{
//            XTaskModel body =  exchange.getMessage().getBody( XTaskModel.class);
//            body.getAmqp_step().setOutput("DATA UNAVAILABLE");
            CompareUsersModel body = setError(exchange, getErrorMessage(err));
            exchange.getMessage().setBody(body);
            exchange.setException(err);
            return exchange;
        };
    }

    public static String getErrorMessage(Throwable t){
        return t.getMessage();

    }

    public static CompareUsersModel setError(Exchange exchange, String output){
        CompareUsersModel body =  exchange.getMessage().getBody( CompareUsersModel.class);
        var  currentTask = (Task<String, String, List<ModelUser>>)body.getCurrentTask();
        currentTask.setErrorMessage(output);
        return body;
    }
    public static CompareUsersModel setBody(Exchange exchange, AMQPModelUserDto[]  output){
        CompareUsersModel body =  exchange.getMessage().getBody( CompareUsersModel.class);
        List<ModelUser> list = Stream.of(output).map(GetUserModel::fromDto).collect(Collectors.toList());
        var  currentTask = (Task<String, String, List<ModelUser>>)body.getCurrentTask();
        currentTask.setOutput(list);
        return body;
    }
}
