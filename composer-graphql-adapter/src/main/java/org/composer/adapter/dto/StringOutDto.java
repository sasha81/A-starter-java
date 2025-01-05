package org.composer.adapter.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StringOutDto {
    private String taskId;
    private String stringContent;
}
