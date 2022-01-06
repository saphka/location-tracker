import org.gradle.api.internal.provider.DefaultProperty

/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    id("com.palantir.docker")
}

val dockerRepository = "saphka"
val dockerImage = "${project.name}:${project.version}"

docker {
    name = dockerImage
    tag("DockerHub", "$dockerRepository/$name")
}

afterEvaluate {
    val bootJarTask = tasks.getByName("bootJar")
    val jarPath: String = (bootJarTask.property("archiveFileName") as DefaultProperty<*>).get() as String

    docker {
        dependencies {
            file("${project.buildDir}/docker-data").mkdirs()
            file("${project.buildDir}/docker-data/Dockerfile").apply {
                writeText(
                    """
                    FROM openjdk:17-alpine
        
                    CMD  [ "-XX:+AlwaysActAsServerClassMachine", "-XX:MaxRAMPercentage=70" ]
        
                    COPY $jarPath /app/application.jar
        
                    ENTRYPOINT [ "java", "-jar", "/app/application.jar"]                
                    """.trimIndent()
                )
            }
        }
        files(
            "${project.buildDir}/docker-data/Dockerfile",
            bootJarTask.property("outputs")
        )
    }
}