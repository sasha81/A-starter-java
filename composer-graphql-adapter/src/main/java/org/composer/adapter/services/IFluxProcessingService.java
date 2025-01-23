package org.composer.adapter.services;

import org.composer.adapter.dto.TaskInput;
import org.composer.adapter.dto.TaskOutput;
import org.composer.core.model.*;
import org.composer.core.utils.Task;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface IFluxProcessingService {

    public Specs getSpecsFromInput(TaskInput input);
    public String getTaskId();


    public Flux<TaskOutput> postProcessContainerFlux(Flux<FluxMessageContainer<?>> inFlux, String taskId, Runnable doOnCancel);
}
