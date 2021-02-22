package org.saphka.locationtracker.user.domain;

import org.saphka.locationtracker.user.domain.dto.UserCreateData;
import org.saphka.locationtracker.user.domain.dto.UserValue;

public interface UserService {

     UserValue createUser(UserCreateData userData);
}
