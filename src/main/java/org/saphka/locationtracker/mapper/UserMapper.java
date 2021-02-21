package org.saphka.locationtracker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.saphka.locationtracker.api.model.UserCreateDTO;
import org.saphka.locationtracker.api.model.UserDTO;
import org.saphka.locationtracker.dao.jooq.tables.records.UserRecord;

@Mapper
public interface UserMapper extends BaseMapper<UserRecord, UserDTO> {

    @Override
    @Mapping(target = "alias", source = "userAlias")
    UserDTO sourceToTarget(UserRecord source);

    @Override
    @Mapping(source = "alias", target = "userAlias")
    UserRecord targetToSource(UserDTO target);

    @Mapping(source = "alias", target = "userAlias")
    UserRecord forCreate(UserCreateDTO source);

}
