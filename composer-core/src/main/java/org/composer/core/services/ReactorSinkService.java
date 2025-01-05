package org.composer.core.services;


import org.composer.core.model.ModelUser;
import org.composer.core.model.*;
import org.composer.core.utils.ISinkMapObjectService;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class ReactorSinkService implements IReactorSinkService {
    Logger logger = LoggerFactory.getLogger(ReactorSinkService.class);

    private final ISinkMapObjectService sinkMapService;

    private final IModelToFlux modelToFlux = new ModelToFlux();
    public ReactorSinkService(ISinkMapObjectService sinkMapService) {
        this.sinkMapService = sinkMapService;
    }


    public void notifyAboutRestStep(Exchange exchange){
        XTaskModel body = exchange.getMessage().getBody(XTaskModel.class);
        String taskId = body.getTask_id();
        String msg;
        if(body.getRest_step().getErrorMessage()==null){
            msg=body.getRest_step().getOutput().stream().map(ModelUser::toString).collect(Collectors.joining(",", "[", "]"));
            sinkMapService.publish(taskId, modelToFlux.getFluxUserRestContainer(body));
        }else{
            msg=body.getRest_step().getErrorMessage();
            sinkMapService.publish(taskId, FluxMessageContainer.builder()
                    .taskId(taskId).stage(ProcessStages.REST).error(msg).build());
        }
        logger.info("RestStep :"+taskId+" "+msg);
   }

    public void notifyAboutAMQPStep(Exchange exchange){
        XTaskModel body = exchange.getMessage().getBody(XTaskModel.class);
        String taskId = body.getTask_id();

        String msg;
        if(body.getAmqp_step().getErrorMessage()==null){
            msg=body.getAmqp_step().getOutput().stream().map(ModelUser::toString).collect(Collectors.joining(",", "[", "]"));
            sinkMapService.publish(taskId, modelToFlux.getFluxUserAMQPContainer(body));
        }else{
            msg=body.getAmqp_step().getErrorMessage();
            sinkMapService.publish(taskId, FluxMessageContainer.builder()
                    .taskId(taskId).stage(ProcessStages.AMQP).error(msg).build());
        }

        logger.info("AMQPStep :"+taskId+" "+msg);
    }

    public void notifyAboutGRPCStep(Exchange exchange){
        XTaskModel body = exchange.getMessage().getBody(XTaskModel.class);
        String taskId = body.getTask_id();

        String msg;
        if(body.getGrpc_step().getErrorMessage()==null){
            msg=body.getGrpc_step().getOutput().stream().map(ModelUser::toString).collect(Collectors.joining(",", "[", "]"));
            sinkMapService.publish(taskId, modelToFlux.getFluxUserGRPCContainer(body) );
        }else{
            msg=body.getGrpc_step().getErrorMessage();
            sinkMapService.publish(taskId, FluxMessageContainer.builder()
                    .taskId(taskId).stage(ProcessStages.GRPC).error(msg).build());
        }

        logger.info("GRPCStep :"+taskId+" "+msg);
    }

    public void notifyAboutFinished(Exchange exchange){
        XTaskModel body = exchange.getMessage().getBody(XTaskModel.class);
        String taskId = body.getTask_id();;
        String msg = "Processing FINISHED";
        sinkMapService.publish(taskId, modelToFlux.getFluxResults(body));
        logger.info("FinishStep :"+taskId+" "+msg);
    }

    public void close(Exchange exchange){
        String taskId = exchange.getMessage().getBody(XTaskModel.class).getTask_id();

        sinkMapService.publish(taskId, FluxMessageContainer.builder()
                .taskId(taskId).stage(ProcessStages.STOP).content(null).build());
    }

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
