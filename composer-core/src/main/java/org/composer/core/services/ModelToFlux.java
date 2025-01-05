package org.composer.core.services;

import org.composer.core.model.*;

import java.util.List;

public class ModelToFlux implements IModelToFlux{

    public FluxMessageContainer<List<ModelUser>> getFluxUserGRPCContainer(XTaskModel body){
        return FluxMessageContainer.<List<ModelUser>>builder()
                .taskId(body.getTask_id()).stage(ProcessStages.GRPC)
                .content(body.getGrpc_step().getOutput())
                .build();
    }
    public FluxMessageContainer<List<ModelUser>> getFluxUserAMQPContainer(XTaskModel body){
        return FluxMessageContainer.<List<ModelUser>>builder()
                .taskId(body.getTask_id()).stage(ProcessStages.AMQP)
                .content(body.getAmqp_step().getOutput())
                .build();
    }
    public FluxMessageContainer<List<ModelUser>> getFluxUserRestContainer(XTaskModel body){
        return FluxMessageContainer.<List<ModelUser>>builder()
                .taskId(body.getTask_id()).stage(ProcessStages.REST)
                .content(body.getRest_step().getOutput())
                .build();
    }

    public FluxMessageContainer<ContainerResults> getFluxResults(XTaskModel body){
        return FluxMessageContainer.<ContainerResults>builder()
                .taskId(body.getTask_id()).stage(ProcessStages.FINISH)
                .content(ContainerResults.builder()
                        .groupsOfTheSameUserMatch(DegreesOfMatching.CLOSE)
                        .numberOfUsersMatch(DegreesOfMatching.DIFFERENT)
                        .build())
                .build();
    }
}
