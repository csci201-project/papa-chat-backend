FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

VOLUME /tmp

EXPOSE 8000

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENV SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
ENV SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/papachat?useSSL=false
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=rootroot

ENTRYPOINT ["java", "-jar", "/app/app.jar"]