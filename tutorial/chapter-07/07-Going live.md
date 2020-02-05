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




[google-closure]: https://clojurescript.org/about/closure
[amcharts]: https://www.amcharts.com/
[google-closure-api]: https://google.github.io/closure-library/api/goog.object.html
<!--stackedit_data:
eyJoaXN0b3J5IjpbNTM5MzcxNzUsLTEzNDczMjAwNzcsLTM1Nz
I4MDE0MywxODY4NjUzNzQ4LDIwMDU0MDI3MTJdfQ==
-->