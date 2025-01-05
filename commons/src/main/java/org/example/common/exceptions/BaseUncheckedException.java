package org.example.common.exceptions;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BaseUncheckedException extends RuntimeException{
    private String code;
    private String message;
    private String module;
    private String language;
    private Object inner;
    private LocalDateTime timestamp;
    private String taskId;

}
