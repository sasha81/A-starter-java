package org.composer.core.services;


import org.composer.core.model.*;
import org.composer.core.utils.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ModelToFluxTest {
    private  final ISpecToModel specToModel= new SpecToModel();
    private final ModelToFlux modelToFluxService= new ModelToFlux();

    @Test
    public void modelToGrpcContainer() throws Exception {
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        String userId = "12345";
        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        CompareUsersModel model = specToModel.getModelFromSpecs(specs);
        model.setNextTask();
        var  currentTask = (Task<String, String, List<ModelUser>>)model.getCurrentTask();
        currentTask.setOutput(userList);
        FluxMessageContainer<List<ModelUser>> result = modelToFluxService.getFluxUserGRPCContainer(model);

        assertEquals(result.getContent(),model.getCurrentTask().getOutput());

    }

    @Test
    public void modelToAmqpContainer() throws Exception {
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        String userId = "12345";
        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        CompareUsersModel model = specToModel.getModelFromSpecs(specs);
        model.setNextTask();
        var  currentTask = (Task<String, String, List<ModelUser>>)model.getCurrentTask();
        currentTask.setOutput(userList);
        FluxMessageContainer<List<ModelUser>> result = modelToFluxService.getFluxUserAMQPContainer(model);

        assertEquals(result.getContent(),model.getCurrentTask().getOutput());

    }

    @Test
    public void modelToRestContainer() throws Exception {
        Specs specs = Specs.builder().specifications("Spec_1").taskId("ABCD").build();
        String userId = "12345";
        ModelGroup group = ModelGroup.builder().userId(userId).groupId("123").groupName("Ggg").build();

        ModelUser input = ModelUser.builder().userId(userId).username("A").userage(15).groups(List.of(group)).build();
        List<ModelUser> userList = List.of(input);
        CompareUsersModel model = specToModel.getModelFromSpecs(specs);
        model.setNextTask();
        var  currentTask = (Task<String, String, List<ModelUser>>)model.getCurrentTask();
        currentTask.setOutput(userList);
        FluxMessageContainer<List<ModelUser>> result = modelToFluxService.getFluxUserRestContainer(model);

        assertEquals(result.getContent(),model.getCurrentTask().getOutput());

    }

}
