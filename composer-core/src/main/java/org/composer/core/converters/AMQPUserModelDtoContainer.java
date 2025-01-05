package org.composer.core.converters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AMQPUserModelDtoContainer implements Serializable {
    private AMQPModelUserDto[] response;
}
