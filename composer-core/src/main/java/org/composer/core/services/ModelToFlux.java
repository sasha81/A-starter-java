package org.composer.core.services;

import org.composer.core.model.*;

import java.util.List;

public class ModelToFlux implements IModelToFlux{

    public FluxMessageContainer<List<ModelUser>> getFluxUserGRPCContainer(CompareUsersModel body){
        return FluxMessageContainer.<List<ModelUser>>builder()
                .taskId(body.getTask_id()).stage(ProcessStages.GRPC)
                .content(body.getGrpc_step().getOutput())
                .build();
    }
    public FluxMessageContainer<List<ModelUser>> getFluxUserAMQPContainer(CompareUsersModel body){
        return FluxMessageContainer.<List<ModelUser>>builder()
                .taskId(body.getTask_id()).stage(ProcessStages.AMQP)
                .content(body.getAmqp_step().getOutput())
                .build();
    }
    public FluxMessageContainer<List<ModelUser>> getFluxUserRestContainer(CompareUsersModel body){
        return FluxMessageContainer.<List<ModelUser>>builder()
                .taskId(body.getTask_id()).stage(ProcessStages.REST)
                .content(body.getRest_step().getOutput())
                .build();
    }

    public FluxMessageContainer<ContainerResults> getFluxResults(CompareUsersModel body){
        return FluxMessageContainer.<ContainerResults>builder()
                .taskId(body.getTask_id()).stage(ProcessStages.RESULT)
                .content(body.getFinal_result().getOutput())
                .build();
    }
}
