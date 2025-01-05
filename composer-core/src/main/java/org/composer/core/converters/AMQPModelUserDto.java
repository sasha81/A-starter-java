package org.composer.core.converters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AMQPModelUserDto {

    String userId ;
    String name ;
    Integer age ;
    List<AMQPModelGroupDto> groups;
}
