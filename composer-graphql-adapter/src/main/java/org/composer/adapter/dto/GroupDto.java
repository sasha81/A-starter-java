package org.composer.adapter.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GroupDto {
    String groupId;
    String groupName;
    String userId;
    Boolean groupStatus;
    Boolean userStatus;
}
