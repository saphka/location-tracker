package org.saphka.location.tracker.commons.test

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@ContextConfiguration(initializers = [AbstractIntegrationTestInitializer::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AbstractIntegrationTest

class AbstractIntegrationTestInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        database.start()
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            applicationContext,
            "spring.r2dbc.url=r2dbc:postgresql://${database.containerIpAddress}:${database.getMappedPort(5432)}/${database.databaseName}",
            "spring.r2dbc.username=${database.username}",
            "spring.r2dbc.password=${database.password}",
            "spring.datasource.url=${database.jdbcUrl}",
            "spring.datasource.username=${database.username}",
            "spring.datasource.password=${database.password}",
            "grpc.port=0"
        )
    }

    companion object {
        private val database =
            TestPostgreSQLContainer(DockerImageName.parse("postgres:13-alpine"))
    }
}

class TestPostgreSQLContainer(image: DockerImageName) : PostgreSQLContainer<TestPostgreSQLContainer>(image)
