package org.saphka.locationtracker.user.domain;

import org.saphka.locationtracker.user.domain.dto.UserCreateData;
import org.saphka.locationtracker.user.domain.dto.UserValue;

public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserFactory userFactory;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository repository, UserFactory userFactory, UserMapper userMapper) {
        this.repository = repository;
        this.userFactory = userFactory;
        this.userMapper = userMapper;
    }

    @Override
    public UserValue createUser(UserCreateData userData) {
        User user = userFactory.make(null);
        user.setAlias(userData.getAlias());
        user.setPublicKey(userData.getPublicKey());
        user.setNewPassword(userData.getPassword());

        return userMapper.toUserValue(repository.save(user));
    }
}
