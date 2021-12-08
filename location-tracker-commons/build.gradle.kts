/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("org.saphka.location.java-conventions")
}

val versions = ext.properties["versions"] as Map<String, String>

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${versions["kotlin"]}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${versions["kotlin"]}")
    implementation("io.jsonwebtoken:jjwt-api:${versions["jjwt"]}")
    implementation("io.jsonwebtoken:jjwt-impl:${versions["jjwt"]}")
    implementation("io.jsonwebtoken:jjwt-jackson:${versions["jjwt"]}")
    implementation("org.springframework.boot:spring-boot-starter:${versions["spring-boot"]}")
    implementation("org.springframework.security:spring-security-config:${versions["spring-security"]}")
    implementation("org.springframework.security:spring-security-oauth2-resource-server:${versions["spring-security"]}")
    implementation("org.springframework.security:spring-security-oauth2-jose:${versions["spring-security"]}")
    implementation("io.projectreactor:reactor-core:${versions["reactor-core"]}")
    implementation("org.jooq:jooq:${versions["jooq"]}")
    implementation("com.zaxxer:HikariCP:${versions["hikari"]}")
    implementation("io.grpc:grpc-stub:${versions["grpc"]}")
    implementation("org.springframework.boot:spring-boot-starter-test:${versions["spring-boot"]}")
    implementation("org.testcontainers:postgresql:${versions["testcontainers"]}")
}

description = "location-tracker-commons"
