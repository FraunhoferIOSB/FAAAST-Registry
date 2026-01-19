FROM eclipse-temurin:17-jdk-alpine
COPY service/target/service-1.2.0-SNAPSHOT.jar service-1.2.0-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/service-1.2.0-SNAPSHOT.jar"]

