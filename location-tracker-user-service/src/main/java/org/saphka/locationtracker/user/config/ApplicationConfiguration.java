package org.saphka.locationtracker.user.config;

import org.saphka.locationtracker.user.domain.*;
import org.saphka.locationtracker.user.mapper.UserMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public UserService userService(UserRepository userRepository, UserFactory userFactory, UserMapper userMapper) {
        return new UserServiceImpl(userRepository, userFactory, userMapper);
    }

    @Bean
    public UserFactory userFactory(PasswordEncoder passwordEncoder) {
        return new UserFactoryImpl(passwordEncoder::encode);
    }

}
