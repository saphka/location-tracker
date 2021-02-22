package org.saphka.locationtracker.user.domain;

import java.util.function.Function;

public class UserFactoryImpl implements UserFactory{

    private final Function<String, String> passwordEncoder;

    public UserFactoryImpl(Function<String, String> passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User make(Integer id) {
        return new User(id, passwordEncoder);
    }
}
