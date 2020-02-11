# Going live

Our application is finished and now it's time to make it live so everyone could try it out. In this chapter we will create a production build, then containerize our application for simpler deployment process, and set up a remote server with a custom domain. 

Code for the beginning of this chapter can be found in  `app/chapter-07/start` folder.

## Production Build

All the configs are already set up for us and we just need to provide a `database-url` for production build in `visitera/env/prod/resources/config.edn` file. So it should look something like that 

```clojure
{:prod true
 :port 3000
 :database-url "datomic:free://localhost:4334/visitera_prod"}
``` 

That's all the preparations that we need. Now we can create a production build using this command:

```
lein uberjar
``` 

If everything is okay it should create a `visitera.jar` file inside `visitera/target/uberjar` folder. Now we can run and test our production ready application using the next command:

```
java -jar ./target/uberjar/visitera.jar
```

Just not to forget that we should have a running database (`bin/transactor config/samples/free-transactor-template.properties`) before we launch our application.

Our app is running so it's time to test it. Let's go to `localhost:3000` and try to create a new user. But after submitting a form we see an error screen. What just happened? Why isn't it working?

To figure out where the problem is we can use logs stored in `visitera/log/visitera.log` file. Somewhere at the end of the file we should be able to find a message that looks like that:

```
Caused by: datomic.impl.Exceptions$IllegalArgumentExceptionInfo: :db.error/not-an-entity Unable to resolve entity: :user/email
```

We're using a new database for production and it seems our database schema hasn't been created. But why? It just worked in dev environment. 

It occurred that the problem was with `install-schema` function from `visitera.db.core` namespace.

```clojure
(defn install-schema
  [conn]
  (for [resource db-resources]
    (let [norms-map (c/read-resource resource)]
      (c/ensure-conforms conn norms-map (keys norms-map)))))
```

Here we're using `for` function to iterate over `db-resources` and do updates to the database. And `for` builds a lazy sequence that by coincidence will be forced to evaluate by REPL when we run our server, in a non-interactive environment nothing will happen though. So to fix this issue we just need to replace `for` with `doseq` which will eagerly execute everything.

```clojure
(defn install-schema
  [conn]
  (doseq [resource db-resources]
    (let [norms-map (c/read-resource resource)]
      (c/ensure-conforms conn norms-map (keys norms-map)))))
```

And now we need to rebuild our application again with:

```
lein uberjar
``` 

And run it with:

```
java -jar ./target/uberjar/visitera.jar
```

Now registration and login should work and we can pass through them to the map screen. 

But another surprise waits us here. If we try to click on the map nothing will happen. And in the browser console we should see an error similar to that one:

```javascript
map.cljs:53 Uncaught TypeError: Cannot read property 'xf' of undefined
    at map.cljs:53
    at Object.dispatch (core.js:1)
    at core.js:1
    at Object.e.each (core.js:1)
    at e.t._eachListener (core.js:1)
    at e.t.dispatchImmediately (core.js:1)
    at e._dispatchSpritePointEvent (core.js:1)
    at Object.dispatch (core.js:1)
    at core.js:1
    at Object.e.each (core.js:1)
```

It seems there is an error in our `../components/map.cljs` component in this line:

```clojure
...
(let [country-id (.. ev -target -dataItem -dataContext -id)
...
```

But we don't have any properties called `xf`. So what's happening here?

To understand what caused that problem let's have a look how our production ready code is being produced. There are a few steps here. First our ClojureScript code is compiled to JavaScript and then it will be aggressively minified by [Google Closure Compiler][google-closure]. 

```
                                      Google Closure
               CLSJ Compiler             Compiler
             -----------------      ------------------
+---------------+         +------------+         +--------------+
| ClojureScript +-------->+ JavaScript +-------->+ Optimized JS |
+---------------+         +------------+         +--------------+
```
During this minification process Google Closure will rename object properties to shorter names. But it knows nothing about [amcharts] library that we used to implement the world map so after minifying  our code all the linkages with that library are broken.  One of the simplest solutions to this problem is to explicitly tell Google Closure not to rename properties that we are using.

So here are the updates that we need to add to `visitera.components.map` namespace:

 1. Import [Google Closure Utils][google-closure-api]

```clojure
(ns visitera.components.map
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [visitera.config :as cfg]
   [goog.object :as g]))
```

  2. Update some functions to preserve property names after compilation process.

```clojure
...
on-country-click (fn [ev]
                   (let [country-id (g/getValueByKeys ev "target" "dataItem" "dataContext" "id")
                         status (keyword (g/getValueByKeys ev "target" "dataItem" "dataContext" "status"))]
                     (rf/dispatch [:update-user-countries {:status (get-next-status status)
                                                           :id country-id}])))
...
update (fn [comp]
         (let [last-updated (second (rest (r/argv comp)))
               polygon (. (g/get @polygon-ref "getPolygonById") call @polygon-ref (:id last-updated))]
           (g/set (g/getValueByKeys polygon "dataItem" "dataContext") "status" (name (:status last-updated))) ;change status
           (set! (.-fill polygon) ((:status last-updated) cfg/colors)))) ;change color
...
```

And as a last step we need to rebuild and test the production build again. This time everything should work as expected. And we can move on to the next step.

## Containerization

To be able to run our application we have a bunch of dependencies *(java, datamomic, leiningen)* installed on a local machine. If we want to move the application to a remote server we'd also have to install all those dependencies. It's not so hard to do this once. But what if we decide to move our application to another server? We'd have to go through this process again and again. 

But there is a great solution called [docker][docker] that will help us to avoid this boring and redundant work of preparing the environment for our application. [Docker][docker] will create an isolated environment and handle all the dependencies based on a configuration file. So dockerizing our application will allow us to run it on any machine just by typing a few commands.  

Here's a simple diagram showing what we want to achieve:

```
         Host                          Host
+----------------------+      +--------------------------+
| +------+ +---------+ |      |          Docker          |
| | Java | | Datomic | |      | +----------------------+ |        
| +------+ +---------+ |      | | +------+ +---------+ | |           
|   ^            ^     | ==>  | | | Java | | Datomic | | |
|   |  +-----+   |     |      | | +------+ +---------+ | |
|   +--+ App +---+     |      | |   ^            ^     | |
|      +-----+         |      | |   |  +-----+   |     | |
+----------------------+      | |   +--+ App +---+     | |
                              | |      +-----+         | |
                              | +----------------------+ |
                              +--------------------------+
```

But before we dockerize the whole application we'll try to simplify our development process and start with creating a docker container for **datomic** so we wouldn't have to download and install it manually.

```
            Host
+-----------------------------+
|             +-------------+ |
|             |   Docker    | |
|             | +---------+ | |
| +------+    | | Datomic | | |
| | Java |    | +---------+ | |
| +------+    +-------------+ |
|   ^                ^        |
|   |  +-----+       |        |
|   +--+ App +-------+        |
|      +-----+                |
+-----------------------------+
```

Before we can start we should [install docker][docker-install] and [install docker-compose][compose-install].

After everything is installed we only need to create a `docker-compose.yml` file that will describe our database container. We will put it inside `visitera/datomic/` folder. So here is the content of it:

```yml
---
version: '3'

services:
  db:
    image: akiel/datomic-free
    ports:     
      - "4334-4336:4334-4336"
    environment: 
      DATOMIC_PASSWORD: datomic
      ADMIN_PASSWORD: admin
    volumes:
      - ./data:/data
      - ./log:/log
```

The config is pretty simple. It says that we're gonna have a service called `db` which will be based on [this image][datomic-image]. We also specify some environment variables and set mappings between host machine and the docker container for some ports and folders.

Now we can run the next command from `visitera/datomic` folder:

```bash
docker-compose up
```

This should start up our database service inside a docker container and make it available to our host machine through shared ports we specified in a config file.

And to test if it works with our application we need to change `:database-url` in `visitera/dev-config.end` file.

```clojurle
...
 :database-url "datomic:free://localhost:4334/visitera_dev?password=datomic"
...
```
And also we should not forget to add these folders to `.gitignore`

```git
/datomic/data
/datomic/log
```

Now we can try to run our application in development mode to make sure that everything works.

We improved our development process a little bit. So now we can get back to a production build and dockerize the whole application. We already created a config for datomic so we are only left with our main clojure application.

Luminus framework already has a default `Dockerfile`:

```docker
FROM openjdk:8-alpine

COPY target/uberjar/visitera.jar /visitera/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/visitera/app.jar"]
``` 

Using it we can run a compiled `jar` file. This will work on our local machine because we have `leiningen` installed and can build our application manually. But for a remote server we want that build process to happen inside docker container so we wouldn't have to install any extra dependencies and just run our application with one command. So here is the updated `Dockerfile` that installs all the dependencies, creates a production build, and runs it:

```docker
FROM clojure

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY project.clj /usr/src/app/

RUN lein deps
COPY . /usr/src/app

RUN lein uberjar
RUN mv ./target/uberjar/visitera.jar /usr/src/app/visitera.jar

CMD ["java", "-jar", "/usr/src/app/visitera.jar"]
```

And now we need to create a `docker-compose.yml` file in the root of our project and connect two docker containers. Here it is:

```yml
---
version: '3'

services:
  db:
    image: akiel/datomic-free
    environment: 
      DATOMIC_PASSWORD: datomic
      ADMIN_PASSWORD: admin
      ALT_HOST: db
    volumes:
      - ./data:/data
      - ./log:/log

  app:
    build: .
    ports:     
      - "3000:3000"
    depends_on: 
      - db
    environment:
      - DATABASE_URL=datomic:free://db:4334/visitera_prod?password=datomic
    volumes:
      - ./log:/log
```

And to run our dockerized application to test it we can use the next command:

```bash
docker-compose up --build
```

After making sure that our application works as expected we only need not to forget to add shared `/log` and `/data` folders to `.gitignore`

```git
/log
/data
```
## Deployment




[google-closure]: https://clojurescript.org/about/closure
[amcharts]: https://www.amcharts.com/
[google-closure-api]: https://google.github.io/closure-library/api/goog.object.html
[docker]: https://www.docker.com/
[docker-install]: https://docs.docker.com/install/
[compose-install]: https://docs.docker.com/compose/install/
[datomic-image]: https://hub.docker.com/r/akiel/datomic-free
<!--stackedit_data:
eyJoaXN0b3J5IjpbMjA5NTkyOTQyOCwtMTAyNjE4MDQ5MiwtMT
I1OTkzODE5MCwtMTA0OTUzOTEyOSwzMTg4NjY1NTIsLTEzNDcz
MjAwNzcsLTM1NzI4MDE0MywxODY4NjUzNzQ4LDIwMDU0MDI3MT
JdfQ==
-->