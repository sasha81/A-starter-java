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
import org.composer.core.model.Specs;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.services.AMQPFutureProcessor;
import org.composer.core.services.ISpecToModel;
import org.composer.core.services.SpecToModel;
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
    private  final ISpecToModel specToModel= new SpecToModel();
    @Test
    public void aMQPProcessorTest() throws Exception {
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();

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


        CompareUsersModel model = specToModel.getModelFromSpecs(specs);
        model.setNextTask();

        exchange.getMessage().setBody(model);

        processor.process(exchange,asyncCamelCallback);
        CompareUsersModel modelOut = exchange.getMessage().getBody(CompareUsersModel.class);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        var  currentTask = (Task<String, String, List<ModelUser>>)modelOut.getCurrentTask();
        assertNotNull(currentTask.getOutput());
        assertEquals(currentTask.getOutput().get(0).getUserId(),userViewDto.getUserId());
        assertEquals(currentTask.getOutput().get(0).getUsername(),userViewDto.getName());
        assertEquals(currentTask.getOutput().get(0).getGroups().get(0).getGroupId(),group.getGroupId());
        assertEquals(currentTask.getOutput().get(0).getGroups().get(0).getUserId(),group.getUserId());
    }

    @Test
    public void aMQPProcessorErrorTest() throws Exception {
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();

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


        Exchange exchange = new DefaultExchange(camelContext);

        String taskId="abcdef";
        String grpcInput= "Ann";

        String amqpInput = "John";
        String restInput = "Mark";
        CompareUsersModel model = specToModel.getModelFromSpecs(specs);
        model.setNextTask();

        exchange.getMessage().setBody(model);

        processor.process(exchange,asyncCamelCallback);
        CompareUsersModel modelOut = exchange.getMessage().getBody(CompareUsersModel.class);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertNull(modelOut.getCurrentTask().getOutput());
        assertTrue(modelOut.getCurrentTask().getErrorMessage().contains(exception.getMessage()));
    }
}
