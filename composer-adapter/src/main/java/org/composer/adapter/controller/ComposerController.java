package org.composer.adapter.controller;

import org.composer.adapter.dto.OutputDto;
import org.composer.adapter.dto.InputDto;
import org.composer.adapter.services.FluxProcessingService;
import org.composer.adapter.services.SendToCamelService;
import org.composer.core.model.FluxMessageContainer;
import org.composer.core.model.Specs;
import org.composer.core.utils.ISinkMapObjectService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping(path="/composerTasks")
public class ComposerController {

    private FluxProcessingService fluxProcessingService;
    private SendToCamelService sendToCamelService;
    private ISinkMapObjectService sinkMapService;

    public ComposerController(ISinkMapObjectService sinkMapService, SendToCamelService sendToCamelService, FluxProcessingService fluxProcessingService) {
       this.sinkMapService = sinkMapService;
        this.sendToCamelService = sendToCamelService;
        this.fluxProcessingService = fluxProcessingService;
    }

    @PostMapping(path = "/compareUsers",  produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<OutputDto>> getEvents(@RequestBody InputDto input ) throws IOException {
        String taskId = fluxProcessingService.getTaskId();


        Flux<FluxMessageContainer<?>> rawFlux = sinkMapService.getNewFluxWithId(taskId);
        Flux<ServerSentEvent<OutputDto>> outFlux = fluxProcessingService.postProcessContainerFlux(rawFlux,taskId,()->sinkMapService.deleteMap(taskId) );

        this.sendToCamelService.sendBodyToCamel("direct:new_Compare_task", Specs
                .builder().specifications(input.getSpecifics()).taskId(taskId).build());
        return outFlux;

    }


}
