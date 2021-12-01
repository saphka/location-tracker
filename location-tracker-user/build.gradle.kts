/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("org.saphka.location.java-conventions")
    id("org.saphka.location.jooq-conventions")
    id("org.saphka.location.protobuf-conventions")
    id("org.saphka.location.docker-conventions")
    id("org.springframework.boot").version("2.5.6")
}

dependencies {
    implementation(project(":location-tracker-commons"))
    implementation(libs.kotlin.lib)
    implementation(libs.kotlin.reflect)
    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.spring.security.config)
    implementation(libs.spring.security.oauth2.res)
    implementation(libs.spring.security.oauth2.jose)
    implementation(libs.grpc.starter)
    implementation(libs.liquibase.core)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.r2dbc.postgresql)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.grpc.client.starter)
    implementation(libs.reactor.core)
}

locationJooq {
    packageName.set("org.saphka.location.tracker.user.dao.jooq")
}

description = "location-tracker-user"
