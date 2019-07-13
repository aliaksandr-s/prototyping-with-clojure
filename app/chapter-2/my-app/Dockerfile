FROM openjdk:8-alpine

COPY target/uberjar/my-app.jar /my-app/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/my-app/app.jar"]
