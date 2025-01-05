package org.composer.adapter.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.composer.core.model.DegreesOfMatching;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContainerResults {
    private DegreesOfMatching numberOfUsersMatch;
    private DegreesOfMatching groupsOfTheSameUserMatch;
}
