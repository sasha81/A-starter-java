package org.composer.adapter.controller;

import org.composer.adapter.dto.OutputDto;
import org.composer.adapter.dto.XTaskDto;
import org.composer.adapter.services.FluxProcessingService;
import org.composer.adapter.services.SendToCamelService;
import org.composer.core.model.FluxMessageContainer;
import org.composer.core.model.XTaskModel;
import org.composer.core.utils.ISinkMapObjectService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping(path="/x-tasks")
public class FluxController {

    private FluxProcessingService fluxProcessingService;
    private SendToCamelService sendToCamelService;
    private ISinkMapObjectService sinkMapService;

    public FluxController(ISinkMapObjectService sinkMapService, SendToCamelService sendToCamelService, FluxProcessingService fluxProcessingService) {
       this.sinkMapService = sinkMapService;
        this.sendToCamelService = sendToCamelService;
        this.fluxProcessingService = fluxProcessingService;
    }

    @PostMapping(path = "/new-x-task",  produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<OutputDto>> getEvents(@RequestBody XTaskDto xTaskDto ) throws IOException {
        String taskId = fluxProcessingService.getTaskId();
        XTaskModel model = fluxProcessingService.getXModelFromDto(xTaskDto, taskId);

        Flux<FluxMessageContainer<?>> rawFlux = sinkMapService.getNewFluxWithId(taskId);
        Flux<ServerSentEvent<OutputDto>> outFlux = fluxProcessingService.postProcessContainerFlux(rawFlux,taskId,()->sinkMapService.deleteMap(taskId) );

        this.sendToCamelService.sendBodyToCamel("direct:new_X_task",model);
        return outFlux;

    }


}
