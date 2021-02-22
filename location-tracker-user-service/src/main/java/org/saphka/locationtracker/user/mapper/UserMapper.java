package org.saphka.locationtracker.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.saphka.locationtracker.user.api.model.UserCreateDTO;
import org.saphka.locationtracker.user.api.model.UserDTO;
import org.saphka.locationtracker.user.dao.jooq.tables.records.UserRecord;
import org.saphka.locationtracker.user.domain.User;
import org.saphka.locationtracker.user.domain.dto.UserCreateData;
import org.saphka.locationtracker.user.domain.dto.UserValue;

@Mapper
public interface UserMapper extends org.saphka.locationtracker.user.domain.UserMapper {

    @Mapping(source = "alias", target = "userAlias")
    @Mapping(source = "password", target = "passwordHash")
    UserRecord toRecord(User user, @MappingTarget UserRecord record);

    @Mapping(target = "alias", source = "userAlias")
    @Mapping(target = "password", source = "passwordHash")
    User toDomain(UserRecord record, @MappingTarget User user);

    UserCreateData toUserCreateData(UserCreateDTO userCreateDTO);

    UserDTO toUserDto(UserValue userValue);
}
