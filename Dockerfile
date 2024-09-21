FROM openjdk:8-jre

ARG jarToCopy

COPY /target/$jarToCopy /app/app.jar
    
CMD ["java","-jar", "/app/app.jar", "mariadb"]