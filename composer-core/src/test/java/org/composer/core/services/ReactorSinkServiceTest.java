package org.composer.core.services;


import org.apache.camel.*;
import org.apache.camel.clock.Clock;
import org.apache.camel.impl.engine.SimpleCamelContext;
import org.apache.camel.spi.UnitOfWork;
import org.apache.camel.support.DefaultExchange;
import org.composer.core.model.FluxMessageContainer;
import org.composer.core.model.ModelGroup;
import org.composer.core.model.ModelUser;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.stubs.SinkObjectServiceStub;
import org.composer.core.utils.ISinkMapObjectService;
import org.composer.core.utils.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ReactorSinkServiceTest {

    private final SinkObjectServiceStub sinkMapService = new SinkObjectServiceStub();
    private final IReactorSinkService reactorSinkService = new ReactorSinkService(sinkMapService);


    private final ModelToFlux modelToFluxService= new ModelToFlux();

    @Test
    public void notifyAboutRestStepNoErrorTest(){
        String taskId = "abcdef"; String userId = "123456";
        String grpcInput = "Ann";
        String restInput = "buddy!";
        String amqpInput = "buddy!";
        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).output(userList).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).output(userList).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).output(userList).build())
                .build();


        sinkMapService.setPublishConsumer((tskId, container)->{
            assertEquals(tskId,taskId);
            assertEquals(container.getContent(),userList);

        });
        CamelContext context = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getMessage().setBody(model);

        reactorSinkService.notifyAboutRestStep(exchange);
    }

    @Test
    public void notifyAboutRestStepWithErrorTest(){
        String taskId = "abcdef"; String userId = "123456";
        String grpcInput = "Ann";
        String restInput = "buddy!";
        String amqpInput = "buddy!";
        String error = "Ooops!";
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).errorMessage(error).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())

                .build();


        sinkMapService.setPublishConsumer((tskId, container)->{
            assertEquals(tskId,taskId);
            assertEquals(container.getError(),error);

        });
        CamelContext context = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getMessage().setBody(model);

        reactorSinkService.notifyAboutRestStep(exchange);
    }
    @Test
    public void notifyAboutGrpcStepNoErrorTest(){
        String taskId = "abcdef"; String userId = "123456";
        String grpcInput = "Ann";
        String restInput = "buddy!";
        String amqpInput = "buddy!";
        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).output(userList).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).output(userList).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).output(userList).build())
                .build();


        sinkMapService.setPublishConsumer((tskId, container)->{
            assertEquals(tskId,taskId);
            assertEquals(container.getContent(),userList);

        });
        CamelContext context = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getMessage().setBody(model);

        reactorSinkService.notifyAboutGRPCStep(exchange);
    }

    @Test
    public void notifyAboutGrpcStepWithErrorTest(){
        String taskId = "abcdef"; String userId = "123456";
        String grpcInput = "Ann";
        String restInput = "buddy!";
        String amqpInput = "buddy!";
        String error = "Ooops!";
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).errorMessage(error).build())

                .build();


        sinkMapService.setPublishConsumer((tskId, container)->{
            assertEquals(tskId,taskId);
            assertEquals(container.getError(),error);

        });
        CamelContext context = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getMessage().setBody(model);

        reactorSinkService.notifyAboutGRPCStep(exchange);
    }
    @Test
    public void notifyAboutAmqpStepNoErrorTest(){
        String taskId = "abcdef"; String userId = "123456";
        String grpcInput = "Ann";
        String restInput = "buddy!";
        String amqpInput = "buddy!";
        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).output(userList).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).output(userList).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).output(userList).build())
                .build();


        sinkMapService.setPublishConsumer((tskId, container)->{
            assertEquals(tskId,taskId);
            assertEquals(container.getContent(),userList);

        });
        CamelContext context = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getMessage().setBody(model);

        reactorSinkService.notifyAboutAMQPStep(exchange);
    }

    @Test
    public void notifyAboutAmqpStepWithErrorTest(){
        String taskId = "abcdef"; String userId = "123456";
        String grpcInput = "Ann";
        String restInput = "buddy!";
        String amqpInput = "buddy!";
        String error = "Ooops!";
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).errorMessage(error).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())

                .build();


        sinkMapService.setPublishConsumer((tskId, container)->{
            assertEquals(tskId,taskId);
            assertEquals(container.getError(),error);

        });
        CamelContext context = new SimpleCamelContext();
        Exchange exchange = new DefaultExchange(context);
        exchange.getMessage().setBody(model);

        reactorSinkService.notifyAboutAMQPStep(exchange);
    }
}
