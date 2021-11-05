package org.saphka.location.tracker.user;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@ContextConfiguration(
        initializers = AbstractIntegrationTest.Initializer.class
)
public class AbstractIntegrationTest {

    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>(DockerImageName.parse("postgres:13-alpine"));

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            database.start();
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.r2dbc.url=r2dbc:postgresql://"
                            + database.getContainerIpAddress() + ":" + database.getMappedPort(5432) + "/" + database.getDatabaseName(),
                    "spring.r2dbc.username=" + database.getUsername(),
                    "spring.r2dbc.password=" + database.getPassword(),
                    "spring.datasource.url=" + database.getJdbcUrl(),
                    "spring.datasource.username=" + database.getUsername(),
                    "spring.datasource.password=" + database.getPassword()
            );
        }
    }

}
