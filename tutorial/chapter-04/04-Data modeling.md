# Data modeling

In this chapter we'll be working with the core part of our application: the database. We need to create a schema for our database and populate it with countires data. 

Code for the beggining of this chapter can be found in  `app/chapter-04/start` folder.

## Preparation

Before we can start working on a schema there are some preparations to make. To conviniently experiment with schema modeling we need a function to reset a database and we want the ability to run it without restarting the whole application. 

But first let's have a look at another way of starting the application. In the previous chapter we used `$ lein run` command which executed `start-app` function from `visitera.core` namespace and run REPL for us. Then we just connected to a running REPL.

Now let's first try to run a REPL using `$ lein repl` command. Then run `(start)`. Now we should have a running app and REPL where we can enter other commands. Let's try to execute `(stop)` command. Now our app is stopped but we still have our REPL. 

That's pretty cool and gives us more flexiblity. But where do these `(start)` and `(stop)` commands come from? That's a good question. All these commands belong to `user` namespace which is located in `/env/dev/clj/user.clj` file. Here its content:

```clojure
(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
    [visitera.config :refer [env]]
    [clojure.spec.alpha :as s]
    [expound.alpha :as expound]
    [mount.core :as mount]
    [visitera.figwheel :refer [start-fw stop-fw cljs]]
    [visitera.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn start 
  "Starts application.
  You'll usually want to run this on startup."
  []
  (mount/start-without #'visitera.core/repl-server))

(defn stop 
  "Stops application."
  []
  (mount/stop-except #'visitera.core/repl-server))

(defn restart 
  "Restarts application."
  []
  (stop)
  (start))
```
We'll add a function to reset 



## Creating a schema
<!--stackedit_data:
eyJoaXN0b3J5IjpbMTM3OTIwMjQ2Ml19
-->