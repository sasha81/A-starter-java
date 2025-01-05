package org.example.common.dto;

import org.example.common.utils.NestPattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class NestAMQPMessage implements Serializable {
    private Object data;
    private NestPattern pattern;
    private String id;
}
