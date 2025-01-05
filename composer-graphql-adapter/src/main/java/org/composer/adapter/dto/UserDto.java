package org.composer.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDto {

    String userId ;
    String name ;
    Integer age ;
    List<GroupDto> groups;
}
