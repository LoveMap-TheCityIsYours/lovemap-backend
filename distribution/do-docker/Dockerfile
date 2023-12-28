FROM openjdk:17-alpine
ENV SPRING_PROFILES_ACTIVE=do
ARG JAR_FILE=build/libs/lovemap-backend-1.19.1.jar
COPY ${JAR_FILE} app.jar
RUN mkdir -p /log
ENTRYPOINT ["java","-jar","/app.jar"]