package org.saphka.locationtracker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.saphka.locationtracker.api.model.UserCreateDTO;
import org.saphka.locationtracker.api.model.UserDTO;
import org.saphka.locationtracker.dao.jooq.tables.records.UsersRecord;

@Mapper
public interface UserMapper extends BaseMapper<UsersRecord, UserDTO> {

    @Override
    @Mapping(target = "alias", source = "userAlias")
    UserDTO sourceToTarget(UsersRecord source);

    @Override
    @Mapping(source = "alias", target = "userAlias")
    UsersRecord targetToSource(UserDTO target);

    @Mapping(source = "alias", target = "userAlias")
    UsersRecord forCreate(UserCreateDTO source);

}
