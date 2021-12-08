import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.spring")
}

repositories {
    mavenLocal()
    mavenCentral()
}

group = "org.saphka.location"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_16

ext {
    set(
        "versions", mapOf(
            "jooq" to "3.15.4",
            "liquibase-core" to "3.10.3",
            "snakeyaml" to "1.28",
            "slf4j" to "1.7.30",
            "protoc" to "3.17.3",
            "grpc" to "1.41.0",
            "grpc-starter" to "4.5.9",
            "kotlin" to "1.5.31",
            "spring-boot" to "2.5.6",
            "spring-security" to "5.5.3",
            "postgresql" to "42.2.24",
            "r2dbc-postgresql" to "0.8.10.RELEASE",
            "reactor-core" to "3.4.11",
            "jjwt" to "0.11.2",
            "hikari" to "4.0.3",
            "testcontainers" to "1.16.2"
        )
    )
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "16"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
    }
}
