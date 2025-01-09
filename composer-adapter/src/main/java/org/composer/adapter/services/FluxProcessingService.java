package org.composer.adapter.services;

import org.composer.adapter.dto.OutputDto;
import org.composer.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class FluxProcessingService {
    Logger logger = LoggerFactory.getLogger(FluxProcessingService.class);
    public String getTaskId(){
        return UUID.randomUUID().toString();
    }


    public Flux<ServerSentEvent<OutputDto>> postProcessContainerFlux(Flux<FluxMessageContainer<?>> inFlux, String taskId, Runnable doOnCancel){
        Flux<ServerSentEvent<OutputDto>> outFlux = inFlux
                .filter(msg-> msg.getTaskId().equals(taskId))
                .takeUntil(msg->msg.getStage().equals(ProcessStages.STOP))
                .map(msg->  ServerSentEvent.<OutputDto> builder()
                                .id(taskId)
                                .data(OutputDto.builder()
                                        .taskId(taskId).stage(msg.getStage())
                                        .content(msg.getContent())
                                        .error(msg.getError()).build())
                                .event(msg.getStage().name())
                                .build())
                .doOnTerminate(() -> {
                    logger.info("doOnTerminate called");
                    doOnCancel.run();})

                ;
        return outFlux;
    }
}
