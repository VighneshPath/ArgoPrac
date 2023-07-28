FROM openjdk:19-jdk

ARG version

COPY libs/micronautguide-$version-all.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]