FROM public.ecr.aws/docker/library/eclipse-temurin:17.0.7_7-jre

ARG JAR_FILE=/build/libs/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]