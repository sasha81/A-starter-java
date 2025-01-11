package org.composer.core.processors;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.engine.SimpleCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.EmptyAsyncCallback;
import org.composer.core.converters.AMQPModelGroupDto;
import org.composer.core.converters.AMQPModelUserDto;
import org.composer.core.converters.AMQPUserModelDtoContainer;
import org.composer.core.model.ModelUser;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.services.AMQPFutureProcessor;
import org.composer.core.stubs.AsyncAmqpTemplateStub;
import org.composer.core.utils.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.amqp.core.AsyncAmqpTemplate;
import org.springframework.amqp.core.Message;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AMQPProcessorTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void aMQPProcessorTest(){
        CountDownLatch latch = new CountDownLatch(1);
        CamelContext camelContext = new SimpleCamelContext();
        String rabbitExchName="abc";
        String rabbitRoutKey="123";


        AMQPModelGroupDto group = AMQPModelGroupDto.builder().groupId("12345")
                .groupName("Physics").userId("ABC").build();
        AMQPModelUserDto userViewDto = AMQPModelUserDto.builder()
                .userId("ABC").name("Sasha")
                .age(30).groups(List.of(group)).build();

        AMQPUserModelDtoContainer amqpUserModelDtoContainer = AMQPUserModelDtoContainer.builder()
                .response(new AMQPModelUserDto[]{userViewDto}).build();

        AsyncCallback asyncCamelCallback = new EmptyAsyncCallback();

        AsyncAmqpTemplate rabbitTemplate = new AsyncAmqpTemplateStub(message->{

            Message outMessage = null;
            try {
                outMessage = new Message(objectMapper.writeValueAsBytes(amqpUserModelDtoContainer));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            CompletableFuture<Message> future = CompletableFuture.completedFuture(outMessage);
            latch.countDown();
            return future;
        });
        AMQPFutureProcessor processor = new AMQPFutureProcessor(rabbitTemplate,rabbitExchName,rabbitRoutKey);


        Exchange exchange = new DefaultExchange(camelContext);

        String taskId="abcdef";
        String grpcInput= "Ann";

        String amqpInput = "John";
        String restInput = "Mark";
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())
                .build();

        exchange.getMessage().setBody(model);

        processor.process(exchange,asyncCamelCallback);
        CompareUsersModel modelOut = exchange.getMessage().getBody(CompareUsersModel.class);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertNotNull(modelOut.getAmqp_step().getOutput());
        assertEquals(modelOut.getAmqp_step().getOutput().get(0).getUsername(),userViewDto.getName());
        assertEquals(modelOut.getAmqp_step().getOutput().get(0).getUserage(),userViewDto.getAge());
        assertEquals(modelOut.getAmqp_step().getOutput().get(0).getUserId(),userViewDto.getUserId());
        assertEquals(modelOut.getAmqp_step().getOutput().get(0).getGroups().size(),userViewDto.getGroups().size());
    }

    @Test
    public void aMQPProcessorErrorTest(){
        CountDownLatch latch = new CountDownLatch(1);
        CamelContext camelContext = new SimpleCamelContext();
        String rabbitExchName="abc";
        String rabbitRoutKey="123";
        String inputMsg = "Sasha";
        String pattern = "{\"cmd\":\"greet\"}";
        String outputMsgStr = "Hello "+inputMsg;
        String errorMsg = "ERROR OCCURRED!";
        AsyncCallback asyncCamelCallback = new EmptyAsyncCallback();
        Throwable exception = new RuntimeException(errorMsg);
        AsyncAmqpTemplate rabbitTemplate = new AsyncAmqpTemplateStub(message->{


            CompletableFuture<Message> future = CompletableFuture.failedFuture(exception);
            latch.countDown();
            return future;
        });
        AMQPFutureProcessor processor = new AMQPFutureProcessor(rabbitTemplate,rabbitExchName,rabbitRoutKey);
//        doAnswer(invocation->{
//            Message msg = invocation.getArgument(2);
//            CompletableFuture<Message> future = CompletableFuture.completedFuture(outMessage);
//            latch.countDown();
//          return future;
//        }).when(rabbitTemplate).sendAndReceive(any(String.class),any(String.class),any(Message.class));

        Exchange exchange = new DefaultExchange(camelContext);

        String taskId="abcdef";
        String grpcInput= "Ann";

        String amqpInput = "John";
        String restInput = "Mark";
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())
                .build();

        exchange.getMessage().setBody(model);

        processor.process(exchange,asyncCamelCallback);
        CompareUsersModel modelOut = exchange.getMessage().getBody(CompareUsersModel.class);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertNull(modelOut.getAmqp_step().getOutput());
        assertTrue(modelOut.getAmqp_step().getErrorMessage().contains(exception.getMessage()));
    }
}
