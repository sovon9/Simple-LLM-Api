FROM eclipse-temurin:17-jdk
LABEL authors="Sovon Singha"

WORKDIR /app

ARG JAVA_JAR

ARG VERSION

COPY $JAVA_JAR app.jar

LABEL version=$VERSION

ENTRYPOINT ["java", "-jar", "app.jar"]