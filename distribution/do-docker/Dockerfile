FROM openjdk:17-alpine
ENV SPRING_PROFILES_ACTIVE=do
ARG JAR_FILE=build/libs/lovemap-backend-1.18.9.jar
COPY ${JAR_FILE} app.jar
RUN mkdir -p /log
ENTRYPOINT ["java","-jar","/app.jar"]