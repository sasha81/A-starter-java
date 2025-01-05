package org.composer.core.converters;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AMQPModelGroupDto {
    String groupId;
    String groupName;
    String userId;
    Boolean groupStatus;
    Boolean userStatus;
}
