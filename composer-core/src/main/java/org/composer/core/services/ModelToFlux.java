package org.composer.core.services;

import org.composer.core.model.*;
import org.composer.core.utils.Task;

import java.util.Collection;
import java.util.List;

public class ModelToFlux implements IModelToFlux{

    public FluxMessageContainer<List<ModelUser>> getFluxUserGRPCContainer(CompareUsersModel body){
        List<ModelUser> list = ((Collection<?>) body.getCurrentTask().getOutput()).stream().map(el-> (ModelUser)el).toList();
        return FluxMessageContainer.<List<ModelUser>>builder()
                .taskId(body.getTask_id()).stage(ProcessStages.GRPC)
                .content(list)
                .build();
    }
    public FluxMessageContainer<List<ModelUser>> getFluxUserAMQPContainer(CompareUsersModel body){
        List<ModelUser> list = ((Collection<?>) body.getCurrentTask().getOutput()).stream().map(el-> (ModelUser)el).toList();
        return FluxMessageContainer.<List<ModelUser>>builder()
                .taskId(body.getTask_id()).stage(ProcessStages.AMQP)
                .content(list)
                .build();
    }
    public FluxMessageContainer<List<ModelUser>> getFluxUserRestContainer(CompareUsersModel body){
        List<ModelUser> list = ((Collection<?>) body.getCurrentTask().getOutput()).stream().map(el-> (ModelUser)el).toList();
        return FluxMessageContainer.<List<ModelUser>>builder()
                .taskId(body.getTask_id()).stage(ProcessStages.REST)
                .content(list )
                .build();
    }

    public FluxMessageContainer<ContainerResults> getFluxResults(CompareUsersModel body){
        var result = (Task<String, String, ContainerResults>) body.getCurrentTask();
        return FluxMessageContainer.<ContainerResults>builder()
                .taskId(body.getTask_id()).stage(ProcessStages.RESULT)
                .content(result.getOutput())
                .build();
    }
}
