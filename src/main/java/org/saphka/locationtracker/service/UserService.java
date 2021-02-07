package org.saphka.locationtracker.service;

import org.saphka.locationtracker.api.model.UserCreateDTO;
import org.saphka.locationtracker.api.model.UserDTO;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import reactor.core.publisher.Mono;

public interface UserService extends ReactiveUserDetailsService {

    Mono<UserDTO> createUser(UserCreateDTO user);
}
