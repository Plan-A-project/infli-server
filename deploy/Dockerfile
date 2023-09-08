FROM eclipse-temurin:17.0.7_7-jre

ARG JAR_FILE=../build/libs/*.jar

COPY ${JAR_FILE} app.jar

ENV TZ=Asia/Seoul

RUN ln -snf /us/share/zoneinfo/STZ /etc/localtime && echo STZ > /etc/timezone

ENTRYPOINT ["java", "-jar", "/app.jar", "-Duser.timezone=Asia/Seoul"]