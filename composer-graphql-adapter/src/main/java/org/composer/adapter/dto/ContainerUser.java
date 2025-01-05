package org.composer.adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.composer.core.model.ModelUser;

import java.util.List;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContainerUser {
    private List<UserDto> userViewContent;
}
