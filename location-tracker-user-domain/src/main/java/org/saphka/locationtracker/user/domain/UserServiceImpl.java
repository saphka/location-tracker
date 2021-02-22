package org.saphka.locationtracker.user.domain;

import org.saphka.locationtracker.user.domain.dto.UserCreateData;
import org.saphka.locationtracker.user.domain.dto.UserValue;

public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserFactory factory;
    private final UserMapper mapper;

    public UserServiceImpl(UserRepository repository, UserFactory factory, UserMapper mapper) {
        this.repository = repository;
        this.factory = factory;
        this.mapper = mapper;
    }

    @Override
    public UserValue createUser(UserCreateData userData) {
        User user = factory.make(null);
        user.setAlias(userData.getAlias());
        user.setPublicKey(userData.getPublicKey());
        user.setNewPassword(userData.getPassword());

        return mapper.toUserValue(repository.save(user));
    }
}
