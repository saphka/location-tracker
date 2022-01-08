plugins {
    id("org.saphka.location.java-conventions")
    id("com.palantir.docker-compose")
}

dependencies {
    docker(project(":location-tracker-user"))
}

dockerCompose {
    setTemplate(file("template.yml"))
}