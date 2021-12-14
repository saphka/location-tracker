import org.gradle.api.internal.provider.DefaultProperty

/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    id("com.palantir.docker")
}

val dockerRepository = "europe-west4-docker.pkg.dev/seventh-chassis-335118/location-tracker"

afterEvaluate {
    val bootJarTask = tasks.getByName("bootJar")
    val jarPath: String = (bootJarTask.property("archiveFileName") as DefaultProperty<*>).get() as String

    docker {
        name = "${dockerRepository}/${project.name}:${project.version}"
        file("${project.buildDir}/Dockerfile").writeText(
            """
            FROM openjdk:17-alpine

            CMD  [ "-XX:+AlwaysActAsServerClassMachine", "-XX:MaxRAMPercentage=70" ]

            COPY $jarPath /app/application.jar

            ENTRYPOINT [ "java", "-jar", "/app/application.jar"]                
            """.trimIndent()
        )
        files(
            "${project.buildDir}/Dockerfile",
            bootJarTask.property("outputs")
        )
    }
}