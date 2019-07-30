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
We'll add a function to reset a database here in a bit. But first let's create a fuction to delete a database in `visitera.db.core` namespace. 

```clojure
(defn delete-database
  []
  (-> env :database-url d/delete-database))
```
Don't forget to import `env` from `visitera.config`

```clojure
[visitera.config :refer [env]]
```

Now we can create a `reset-db` function in `user` namespace. It just deletes a database and restrarts our application. We also added `(install-schema conn)` to `start` function as we did in previous chapter with `visitera.core` namespace. Here's an updated file:

```clojure
(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
   [visitera.config :refer [env]]
   [clojure.spec.alpha :as s]
   [expound.alpha :as expound]
   [mount.core :as mount]
   [visitera.figwheel :refer [start-fw stop-fw cljs]]
   [visitera.core :refer [start-app]]
   [visitera.db.core :refer [conn install-schema delete-database]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn start 
  "Starts application.
  You'll usually want to run this on startup."
  []
  (mount/start-without #'visitera.core/repl-server)
  (install-schema conn))

(defn stop 
  "Stops application."
  []
  (mount/stop-except #'visitera.core/repl-server))

(defn restart 
  "Restarts application."
  []
  (stop)
  (start))

(defn reset-db
  "Delete database and restart application"
  []
  (delete-database)
  (restart))
```

Now let's restart our REPL. We can stop it with <kbd>CTRL</kbd>+<kbd>D</kbd> command. Then `$ lein repl` and `(start)`. Now we can try to execute `(reset-db)`. Everything should work, but let's verify to be sure. 

In the previous chapter we added `/db-test` route to `visitera.routes.home` namespace which just returns a name of a user with `abc` id which is equal to `Good Name A`
 
```clojure
["/db-test" {:get (fn [_]
                       (let [db (d/db conn)
                             user (find-user db "abc")]
                         (-> (response/ok (:user/name user))
                             (response/header "Content-Type" "text/plain; charset=utf-8"))))}]
```

So let's open `/resources/migrations/schema.edn` and change its name to something like `Bad Name B` or any other stupid name that you can imagine.

```clojure
{:user/id     "abc"
 :user/name   "Bad Name B"
 :user/email  "abc@example.com"
 :user/status :user.status/active}
```
Now simply run `(reset-db)` in our REPL and have a look at the result by the address `http://localhost:3000/db-test`

## Creating a schema

Now we have everything ready to start working on our database schema. From the data perspective our app is quite simple. We have users and countries and some countries will be related to some users. 

Let's start with a user model. First of all we'll use an email as a unique identifier `:user/email`. Then we need a password `:user/password` (we'll be storing a hash in the database). And to associate countries we'll use a few lists `:user/countries-visited` and `:user/countries-to-visit`.

For countries we definitely need a name `:country/name`. And... and... what are the other attributes we might need? Let's do some research and see what data format our client code would use. I was able to find these [maps][datamaps] and they use **alpha-3** codes to colorize countries. So let's add `:country/alpha-3` attribute and a numerical code `:country/code` just in case.

That's how our `resources/migrations/schema.edn` file should look like:

```clojure
{;; norm1 installs the schema into Datomic
 :visitera/norm1
 {:txes
  [[;; User schema
    {:db/doc                "User email address"
     :db/ident              :user/email
     :db/valueType          :db.type/string
     :db/cardinality        :db.cardinality/one
     :db/unique             :db.unique/identity}

    {:db/doc                "User password hash"
     :db/ident              :user/password
     :db/valueType          :db.type/string
     :db/cardinality        :db.cardinality/one}

    {:db/doc                "Countries user already visited"
     :db/ident              :user/countries-visited
     :db/valueType          :db.type/ref
     :db/cardinality        :db.cardinality/many}

    {:db/doc                "Countries user wants to visit"
     :db/ident              :user/countries-to-visit
     :db/valueType          :db.type/ref
     :db/cardinality        :db.cardinality/many}]

    ;; Country schema
   [{:db/doc                "Country name"
     :db/ident              :country/name
     :db/valueType          :db.type/string
     :db/cardinality        :db.cardinality/one
     :db/unique             :db.unique/identity}

    {:db/doc                "Country ISO alpha-3 code"
     :db/ident              :country/alpha-3
     :db/valueType          :db.type/string
     :db/cardinality        :db.cardinality/one}

    {:db/doc                "Country code"
     :db/ident              :country/code
     :db/valueType          :db.type/string
     :db/cardinality        :db.cardinality/one}]]}}
```

Now let's have a look at some common attributes we used here.

 - `:db/doc` is an optional documentation string.
 - `:db/ident` specifies a unique name for an attribute
 - `:db/valueType` specifies the type of data that can be stored in the attribute
 - `:db/cardinality` specifies whether the attribute stores a single value, or a collection of values
 - `:db/unique` specifies a uniqueness constraint for the values of an attribute

Here are a few articles from official docs that have more information about [Datomic data model][datomic-data-model] and [Datomic schema][datomic-schema].

And now we can run `(reset-db)` to apply our new changes. To verify that everything worked we can run a `show-schema` function from `visitera.db.core` namespace or we can try to use a gui solution. Here is a link to [download datomic console][datomic-console-download] (a gui for datomic). After downloading follow the instuctions in `README.MD` file. And here is [a link from docs][datomic-console-docs] that shows how to use it.



[datamaps]: https://datamaps.github.io/
[datomic-data-model]: https://docs.datomic.com/cloud/whatis/data-model.html
[datomic-schema]: https://docs.datomic.com/cloud/schema/schema-reference.html
[datomic-console-download]: https://my.datomic.com/downloads/console
[datomic-console-docs]: https://docs.datomic.com/on-prem/console.html
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTE2MTE2MDY1NywxMDE1NDA1NjExLDM1NT
EwMDAzOF19
-->