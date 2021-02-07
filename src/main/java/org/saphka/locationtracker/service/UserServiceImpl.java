package org.saphka.locationtracker.service;

import org.saphka.locationtracker.api.model.UserCreateDTO;
import org.saphka.locationtracker.api.model.UserDTO;
import org.saphka.locationtracker.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Autowired
    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return repository
                .getUserByIAlias(username)
                .map(record -> User
                        .withUsername(record.getUserAlias())
                        .password(record.getPasswordHash())
                        .build()
                );
    }

    @Override
    public Mono<UserDTO> createUser(UserCreateDTO user) {
        return null;
    }
}
