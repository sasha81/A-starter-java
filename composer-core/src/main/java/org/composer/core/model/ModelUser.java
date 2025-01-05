package org.composer.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ModelUser  implements Serializable {
    String userId ;
    String username ;
    Integer userage ;
    List<ModelGroup> groups;

    @Override
    public String toString(){
        return "{userId: " + userId+ ", userName: " + username +", userAge: "+userage+ ", groups: " + groups.stream().map(ModelGroup::toString).collect(Collectors.joining(",", "[", "]")) + "}";
    }
}
