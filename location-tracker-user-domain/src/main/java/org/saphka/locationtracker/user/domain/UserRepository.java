package org.saphka.locationtracker.user.domain;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByAlias(String alias);
}
