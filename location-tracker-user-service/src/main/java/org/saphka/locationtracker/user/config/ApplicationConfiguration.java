package org.saphka.locationtracker.user.config;

import org.jooq.conf.RenderNameCase;
import org.jooq.conf.Settings;
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

    @Bean
    public Settings jooqSettings() {
        return new Settings()
                .withRenderNameCase(RenderNameCase.LOWER);
    }

}
