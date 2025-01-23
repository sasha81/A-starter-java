package org.composer.core.services;


import org.composer.core.model.*;
import org.composer.core.utils.ISinkMapObjectService;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class ReactorSinkService implements IReactorSinkService {
    Logger logger = LoggerFactory.getLogger(ReactorSinkService.class);

    private final ISinkMapObjectService sinkMapService;

    private final IModelToFlux modelToFlux = new ModelToFlux();
    public ReactorSinkService(ISinkMapObjectService sinkMapService) {
        this.sinkMapService = sinkMapService;
    }


    @Override
    public void notifyAboutCreateStep(Exchange exchange) {
        String taskId = (String)exchange.getMessage().getHeader("id");
        String msg="Compare User Task "+taskId+" successfully started";
        String errMsg =(String) exchange.getMessage().getHeader("initError");
        if(errMsg==null || errMsg.isEmpty()){
            sinkMapService.publish(taskId, FluxMessageContainer.builder()
                    .taskId(taskId).stage(ProcessStages.CREATE).content(msg).build());
        }else{
            sinkMapService.publish(taskId, FluxMessageContainer.builder()
                    .taskId(taskId).stage(ProcessStages.CREATE).error(errMsg).build());
        }
    }

    public void notifyAboutRestStep(Exchange exchange){
        CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
        String taskId = body.getTask_id();
        String msg;
        if(body.getCurrentTask().getErrorMessage()==null){

            sinkMapService.publish(taskId, modelToFlux.getFluxUserRestContainer(body));
        }else{
            msg=body.getCurrentTask().getErrorMessage();
            sinkMapService.publish(taskId, FluxMessageContainer.builder()
                    .taskId(taskId).stage(ProcessStages.REST).error(msg).build());
        }

   }

    public void notifyAboutAMQPStep(Exchange exchange){
        CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
        String taskId = body.getTask_id();

        String msg;
        if(body.getCurrentTask().getErrorMessage()==null){

            sinkMapService.publish(taskId, modelToFlux.getFluxUserAMQPContainer(body));
        }else{
            msg=body.getCurrentTask().getErrorMessage();
            sinkMapService.publish(taskId, FluxMessageContainer.builder()
                    .taskId(taskId).stage(ProcessStages.AMQP).error(msg).build());
        }
    }

    public void notifyAboutGRPCStep(Exchange exchange){
        CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
        String taskId = body.getTask_id();

        String msg;
        if(body.getCurrentTask().getErrorMessage()==null){

            sinkMapService.publish(taskId, modelToFlux.getFluxUserGRPCContainer(body) );
        }else{
            msg=body.getCurrentTask().getErrorMessage();
            sinkMapService.publish(taskId, FluxMessageContainer.builder()
                    .taskId(taskId).stage(ProcessStages.GRPC).error(msg).build());
        }

    }

    public void notifyAboutResultStep(Exchange exchange){
        CompareUsersModel body = exchange.getMessage().getBody(CompareUsersModel.class);
        String taskId = body.getTask_id();;
        String msg = "Processing RESULT";
        sinkMapService.publish(taskId, modelToFlux.getFluxResults(body));
        logger.info("ResultStep :"+taskId+" "+msg);
    }

    public void close(Exchange exchange){
        String taskId = (String)exchange.getMessage().getHeader("id");

        sinkMapService.publish(taskId, FluxMessageContainer.builder()
                .taskId(taskId).stage(ProcessStages.STOP).content(null).build());
    }

}
