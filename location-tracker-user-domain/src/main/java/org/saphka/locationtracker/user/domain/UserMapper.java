package org.saphka.locationtracker.user.domain;

import org.saphka.locationtracker.user.domain.dto.UserValue;

public interface UserMapper {

    UserValue toUserValue(User user);

}
