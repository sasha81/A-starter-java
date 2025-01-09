package org.composer.core.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Specs {
    private String specifications;
    private String taskId;
}
