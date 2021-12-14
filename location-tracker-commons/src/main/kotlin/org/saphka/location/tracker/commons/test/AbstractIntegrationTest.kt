package org.saphka.location.tracker.commons.test

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [AbstractIntegrationTestInitializer::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AbstractIntegrationTest {
    companion object {
        val database =
            TestPostgresSQLContainer(DockerImageName.parse("postgres:13-alpine"))
    }

}


class AbstractIntegrationTestInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val database = AbstractIntegrationTest.database
        database.start()
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            applicationContext,
            "spring.r2dbc.url=r2dbc:postgresql://${database.containerIpAddress}:${database.getMappedPort(5432)}/${database.databaseName}",
            "spring.r2dbc.username=${database.username}",
            "spring.r2dbc.password=${database.password}",
            "spring.datasource.url=${database.jdbcUrl}",
            "spring.datasource.username=${database.username}",
            "spring.datasource.password=${database.password}",
            "grpc.port=0",
            "jwt.keystore=classpath:keystore/token-dev.jks",
            "jwt.keystore-password=verysecret"
        )
    }

}

class TestPostgresSQLContainer(image: DockerImageName) : PostgreSQLContainer<TestPostgresSQLContainer>(image)
