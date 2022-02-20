plugins {
    id("org.saphka.location.java-conventions")
    id("org.saphka.location.jooq-conventions")
    id("org.saphka.location.protobuf-conventions")
    id("org.springframework.boot").version("2.5.6")
}

val versions = ext.properties["versions"] as Map<String, String>

dependencies{
    implementation(project(":location-tracker-commons"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${versions["kotlin"]}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${versions["kotlin"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${versions["kotlin-coroutine"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:${versions["kotlin-coroutine"]}")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:${versions["spring-boot"]}")
    implementation("org.springframework.security:spring-security-config:${versions["spring-security"]}")
    implementation("org.springframework.security:spring-security-oauth2-resource-server:${versions["spring-security"]}")
    implementation("org.springframework.security:spring-security-oauth2-jose:${versions["spring-security"]}")
    implementation("io.github.lognet:grpc-spring-boot-starter:${versions["grpc-starter"]}")
    implementation("org.liquibase:liquibase-core:${versions["liquibase-core"]}")
    runtimeOnly("org.postgresql:postgresql:${versions["postgresql"]}")
    runtimeOnly("io.r2dbc:r2dbc-postgresql:${versions["r2dbc-postgresql"]}")
    testImplementation("org.springframework.boot:spring-boot-starter-test:${versions["spring-boot"]}")
}

locationJooq {
    packageName.set("org.saphka.location.tracker.location.dao.jooq")
}

tasks.bootJar.get().archiveFileName.set("app.jar")

description = "location-tracker-location"
