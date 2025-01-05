package org.composer.core.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class FluxMessageContainer<O> implements Serializable {
    private ProcessStages stage;
    private String taskId;
    private O content;
    private String error;
}
