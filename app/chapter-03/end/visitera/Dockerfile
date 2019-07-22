FROM openjdk:8-alpine

COPY target/uberjar/visitera.jar /visitera/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/visitera/app.jar"]
