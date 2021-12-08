FROM openjdk:17-alpine

CMD  [ "-XX:+AlwaysActAsServerClassMachine", "-XX:MaxRAMPercentage=70" ]

ARG JAR_FILE

COPY ${JAR_FILE} /app/application.jar

ENTRYPOINT [ "java", "-jar", "/app/application.jar"]