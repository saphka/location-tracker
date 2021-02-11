package org.saphka.locationtracker.service;

import org.saphka.locationtracker.api.model.UserCreateDTO;
import org.saphka.locationtracker.api.model.UserDTO;
import org.saphka.locationtracker.dao.UserRepository;
import org.saphka.locationtracker.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository repository, UserMapper mapper, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return repository
                .getUserByAlias(username)
                .map(record -> User
                        .withUsername(record.getUserAlias())
                        .password(record.getPasswordHash())
                        .build()
                );
    }

    @Override
    public Mono<UserDTO> createUser(UserCreateDTO user) {
        return Mono.just(user)
                .map(mapper::forCreate)
                .map(r -> {
                    r.setPasswordHash(passwordEncoder.encode(user.getPassword()));
                    return r;
                })
                .flatMap(repository::create)
                .map(mapper::sourceToTarget);
    }
}
