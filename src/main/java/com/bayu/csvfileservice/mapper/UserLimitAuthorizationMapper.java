package com.bayu.csvfileservice.mapper;

import com.bayu.csvfileservice.dto.authorization.UserLimitAuthorizationDto;
import com.bayu.csvfileservice.dto.authorization.UserLimitAuthorizationRequest;
import com.bayu.csvfileservice.model.UserLimitAuthorization;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserLimitAuthorizationMapper {

    default UserLimitAuthorizationDto fromEntityToDto(UserLimitAuthorization entity) {
        if (entity == null) {
            return null;
        }

        return UserLimitAuthorizationDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .userGroup(entity.getUserGroup())
                .limit(entity.getLimit() != null ? entity.getLimit().toPlainString() : null)
                .build();
    }

    List<UserLimitAuthorizationDto> fromEntitiesToDtos(List<UserLimitAuthorization> entities);

    UserLimitAuthorizationDto fromRequestToDto(UserLimitAuthorizationRequest request);

}