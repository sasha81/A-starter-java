package org.composer.adapter.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.composer.core.model.ProcessStages;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutputDto implements Serializable {
    private String taskId;
    private ProcessStages stage;
    private Object content;
    private String error;
}
