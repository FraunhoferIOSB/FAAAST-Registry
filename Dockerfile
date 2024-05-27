FROM eclipse-temurin:17-jdk-alpine
COPY service/target/service-0.1.0-SNAPSHOT.jar service-0.1.0-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/service-0.1.0-SNAPSHOT.jar"]
