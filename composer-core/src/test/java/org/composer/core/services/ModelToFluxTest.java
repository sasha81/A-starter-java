package org.composer.core.services;


import org.composer.core.model.FluxMessageContainer;
import org.composer.core.model.ModelUser;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.utils.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ModelToFluxTest {

    private final ModelToFlux modelToFluxService= new ModelToFlux();

    @Test
    public void modelToGrpcContainer(){
        String taskId = "abcdef";
        String grpcInput = "Ann";
        String restInput = "buddy!";
        String amqpInput = "buddy!";
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())
                .build();
        FluxMessageContainer<List<ModelUser>> result = modelToFluxService.getFluxUserGRPCContainer(model);

        assertEquals(result.getContent(),model.getGrpc_step().getOutput());

    }

    @Test
    public void modelToAmqpContainer(){
        String taskId = "abcdef";
        String grpcInput = "Ann";
        String restInput = "buddy!";
        String amqpInput = "buddy!";
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())
                .build();
        FluxMessageContainer<List<ModelUser>> result = modelToFluxService.getFluxUserAMQPContainer(model);

        assertEquals(result.getContent(),model.getAmqp_step().getOutput());

    }

    @Test
    public void modelToRestContainer(){
        String taskId = "abcdef";
        String grpcInput = "Ann";
        String restInput = "buddy!";
        String amqpInput = "buddy!";
        CompareUsersModel model = CompareUsersModel.builder()
                .task_id(taskId)
                .rest_step(Task.<String, String, List<ModelUser>>builder().input(restInput).build())
                .amqp_step(Task.<String, String, List<ModelUser>>builder().input(amqpInput).build())
                .grpc_step(Task.<String, String, List<ModelUser>>builder().input(grpcInput).build())
                .build();
        FluxMessageContainer<List<ModelUser>> result = modelToFluxService.getFluxUserRestContainer(model);

        assertEquals(result.getContent(),model.getRest_step().getOutput());

    }

}
