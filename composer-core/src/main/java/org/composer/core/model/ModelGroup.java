package org.composer.core.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import users.Users;

import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ModelGroup implements Serializable {
    String groupId;
    String groupName;
    String userId;
    Boolean groupStatus;
    Boolean userStatus;

    @Override
    public String toString(){
        return "{groupId:" + groupId + ", groupName: " + groupName + ", userId: "+userId+", groupStatus: "+groupStatus+", userStatus: "+userStatus + "}";
    }
}
