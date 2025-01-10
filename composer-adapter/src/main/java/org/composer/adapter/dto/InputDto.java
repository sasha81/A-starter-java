package org.composer.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InputDto implements Serializable {
    private String rest_input;
    private String amqp_input;
    private String grpc_input;
}
