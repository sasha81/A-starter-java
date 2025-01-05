package org.composer.core.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContainerResults {
    private DegreesOfMatching numberOfUsersMatch;
    private DegreesOfMatching groupsOfTheSameUserMatch;
}
