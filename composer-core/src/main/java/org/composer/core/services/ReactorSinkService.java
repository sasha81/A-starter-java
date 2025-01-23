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
        CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
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
        CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
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
        CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
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

    public void notifyAboutResult(Exchange exchange){
        CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
        String taskId = body.getTask_id();;
        String msg = "Result is ready";
        sinkMapService.publish(taskId, modelToFlux.getFluxResults(body));
        logger.info("ResultStep :"+taskId+" "+msg);
    }

    public void close(Exchange exchange){
        String taskId = exchange.getMessage().getBody(CompareUsersModel.class).getTask_id();

        sinkMapService.publish(taskId, FluxMessageContainer.builder()
                .taskId(taskId).stage(ProcessStages.STOP).content(null).build());
    }

}
