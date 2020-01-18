FROM clojure

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY project.clj /usr/src/app/

RUN lein deps
COPY . /usr/src/app

RUN lein uberjar
RUN mv ./target/uberjar/visitera.jar /usr/src/app/visitera.jar

CMD ["java", "-jar", "/usr/src/app/visitera.jar"]