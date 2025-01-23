package org.composer.adapter.controller;

import org.composer.adapter.dto.OutputDto;
import org.composer.adapter.dto.InputDto;
import org.composer.adapter.services.FluxProcessingService;
import org.composer.adapter.services.SendToCamelService;
import org.composer.core.model.FluxMessageContainer;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.routes.UserRouteNames;
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
    public Flux<ServerSentEvent<OutputDto>> getEvents(@RequestBody InputDto inputDto) throws IOException {
        String taskId = fluxProcessingService.getTaskId();
        CompareUsersModel model = fluxProcessingService.getXModelFromDto(inputDto, taskId);

        Flux<FluxMessageContainer<?>> rawFlux = sinkMapService.getNewFluxWithId(taskId);
        Flux<ServerSentEvent<OutputDto>> outFlux = fluxProcessingService.postProcessContainerFlux(rawFlux,taskId,()->sinkMapService.deleteMap(taskId) );

        this.sendToCamelService.sendBodyToCamel("direct:"+ UserRouteNames.NEW_COMPARE_USERS.name,model);
        return outFlux;

    }


}
