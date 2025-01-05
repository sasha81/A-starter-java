package org.composer.core.converters;

import org.composer.core.model.ModelGroup;
import org.composer.core.model.ModelUser;
import users.Users;

import java.util.stream.Collectors;

public class GetUserModel {
    public static ModelUser fromDto(Users.UserViewDto dto){
        return ModelUser.builder()
                .groups(dto.getGroupsList().stream().map(GetUserModel::fromDto).collect(Collectors.toList()))
                .userId(dto.getUserId())
                .username(dto.getUsername())
                .userage(dto.getUserage())
                .build();
    }
    public static ModelGroup fromDto(Users.Group group){
        return ModelGroup.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupname())
                .groupStatus(group.getGroupstatus())
                .userId(group.getUserId())
                .userStatus(group.getGroupstatus())
                .build();
    }
    public static ModelGroup fromDto(AMQPModelGroupDto group){
        return  ModelGroup.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .groupStatus(group.getGroupStatus())
                .userId(group.getUserId())
                .userStatus(group.getGroupStatus())
                .build();
    }
    public static ModelGroup fromDto(RestModelGroupDto group){
        return  ModelGroup.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .groupStatus(group.getGroupStatus())
                .userId(group.getUserId())
                .userStatus(group.getGroupStatus())
                .build();
    }
    public static ModelUser fromDto(AMQPModelUserDto dto){
        return ModelUser.builder()
                .groups(dto.getGroups().stream().map(GetUserModel::fromDto).collect(Collectors.toList()))
                .userId(dto.getUserId())
                .username(dto.getName())
                .userage(dto.getAge())
                .build();
    }
    public static ModelUser fromDto(RestModelUserDto dto){
        return ModelUser.builder()
                .groups(dto.getGroups().stream().map(GetUserModel::fromDto).collect(Collectors.toList()))
                .userId(dto.getUserId())
                .username(dto.getName())
                .userage(dto.getAge())
                .build();
    }
}
