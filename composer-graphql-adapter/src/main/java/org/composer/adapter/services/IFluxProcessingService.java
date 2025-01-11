package org.composer.adapter.services;

import org.composer.adapter.dto.TaskInput;
import org.composer.adapter.dto.TaskOutput;
import org.composer.core.model.FluxMessageContainer;
import org.composer.core.model.ModelUser;
import org.composer.core.model.ProcessStages;
import org.composer.core.model.CompareUsersModel;
import org.composer.core.utils.Task;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface IFluxProcessingService {
    public String getTaskId();

    public CompareUsersModel getXModelFromDto(TaskInput dto, String taskId);
    public Flux<TaskOutput> postProcessContainerFlux(Flux<FluxMessageContainer<?>> inFlux, String taskId, Runnable doOnCancel);
}
