/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("org.saphka.location.java-conventions")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.31")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.31")
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.2")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.2")
    implementation("org.springframework.boot:spring-boot-starter:2.5.6")
    implementation("org.springframework.security:spring-security-config:5.5.3")
    implementation("org.springframework.security:spring-security-oauth2-resource-server:5.5.3")
    implementation("org.springframework.security:spring-security-oauth2-jose:5.5.3")
    implementation("io.projectreactor:reactor-core:3.4.11")
    implementation("io.grpc:grpc-stub:1.41.0")
    implementation("org.jooq:jooq:3.15.4")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.springframework.boot:spring-boot-starter-test:2.5.6")
    implementation("org.testcontainers:postgresql:1.16.2")
}

description = "location-tracker-commons"