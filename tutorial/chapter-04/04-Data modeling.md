# Data modeling

In this chapter we'll be working with the core part of our application: the database. We need to create a schema for our database, populate it with countries data and add some queries. 

Code for the beginning of this chapter can be found in  `app/chapter-04/start` folder.

## Preparation

Before we can start working on a schema there are some preparations to make. To conveniently experiment with schema modeling we need a function to reset a database and we want the ability to run it without restarting the whole application. 

But first let's have a look at another way of starting the application. In the previous chapter we used `$ lein run` command which executed `start-app` function from `visitera.core` namespace and run REPL for us. Then we just connected to a running REPL.

Now let's first try to run a REPL using `$ lein repl` command. Then run `(start)`. Now we should have a running app and REPL where we can enter other commands. Let's try to execute `(stop)` command. Now our app is stopped but we still have our REPL. 

That's pretty cool and gives us more flexibility. But where do these `(start)` and `(stop)` commands come from? That's a good question. All these commands belong to `user` namespace which is located in `/env/dev/clj/user.clj` file. Here its content:

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

Now we can create a `reset-db` function in `user` namespace. It just deletes a database and restarts our application. We also added `(install-schema conn)` to `start` function as we did in previous chapter with `visitera.core` namespace. Here's an updated file:

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

And now we can run `(reset-db)` to apply our new changes. To verify that everything worked we can run a `show-schema` function from `visitera.db.core` namespace or we can try to use a GUI solution. Here is a link to [download datomic console][datomic-console-download] (a GUI for datomic). After downloading follow the instructions in `README.MD` file. And here is [a link from docs][datomic-console-docs] that shows how to use it. After installing and launching in should be available in your browser by that address: `http://localhost:8080/browse`

## Prepopulating countries data

For our application to function properly we need to have information about all the countries prepopulated in the database. We definitely don't want to do this by hand, so let's do some research and try to find that data in some format we could use.

After some researches I was able to find that [Countries list project][countries-list-github]. It has everything we need and even more. Here is a [json file][countries-list-json] with all the countries and codes. And because we're in a clojure world we need to convert json to edn. I used this [json to edn converter][json-to-edn-converter].

That's what we had:

```json
[{
"name": "Afghanistan",
"alpha-3": "AFG",
"country-code": "004"
},{
"name": "Åland Islands",
"alpha-3": "ALA",
"country-code": "248"
},{
"name": "Albania",
"alpha-3": "ALB",
"country-code": "008"
} ... ]
```

And that's what we got:

```clojure
[{:name "Afghanistan",
  :alpha-3 "AFG",
  :country-code "004"}
 {:name "Åland Islands",
  :alpha-3 "ALA",
  :country-code "248"}
 {:name "Albania",
  :alpha-3 "ALB",
  :country-code "008"} ... ]
```

We only need to change keys to country entity attributes, and add the full list to `resources/migrations/schema.edn`. 

```clojure
:visitera/data1
 {:txes
  [[{:country/name "Afghanistan"
     :country/alpha-3 "AFG"
     :country/code "004"}
    {:country/name "Åland Islands"
     :country/alpha-3 "ALA"
     :country/code "248"}
    {:country/name "Albania"
     :country/alpha-3 "ALB"
     :country/code "008"}
  ... ]]}
```

Sure we could have put all this data to a separate file but for simplicity we put everything in one. So as an **exercise** you may try to do some refactoring to `install-schema` function from `visitera.db.core` namespace so we would have one file just for schema and another one for countries data.

Don't forget to run `(reset-db)` from terminal to apply all the changes.

## Querying the Database

We've just created a schema and added countries data to the database, so now it's time to add some queries. 

First let's open `src/clj/visitera/db/core.clj` file and replace `add-user` and `find-user` functions.

```clojure
(defn add-user
  "Adds new user to a database"
  [conn {:keys [email password]}]
  (when-not (find-one-by (d/db conn) :user/email email)
    @(d/transact conn [{:user/email    email
                        :user/password password}])))

(defn find-user [db email]
  "Find user by email"
  (d/touch (find-one-by db :user/email email)))
```

We need `when-not` part to make sure we're adding a new entity and not modifying the old one.

Let's run these functions to check that everything works:

```clojure
(add-user conn {:email    "test@user.com"
                :password "somepass"})

(find-user (d/db conn) "test@user.com")
```

As a response we should get something like this:

```clojure
{ :db/id 17592186045673,
  :user/email "test@user.com", 
  :user/password "somepass" }
```

`:db/id` attribute is added automatically by datomic.

Next we need a few functions to add and remove countries to `:user/countries-visited` and `:user/countries-to-visit` lists. We expect them to be called that way:

```clojure
(remove-from-countries :visited conn "test@user.com" "BLR")
(add-to-countries :to-visit conn "test@user.com" "BLR")
```

As a first argument we pass a type of list, then connection to the database, user email, and alpha-3 code. 

To get country id from alpha-3 code we'll use that helper function:

```clojure
(defn get-country-id-by-alpha-3 [db alpha-3]
  (-> (find-one-by db :country/alpha-3 alpha-3)
      (d/touch)
      (:db/id)))
```

And we need a helper function to get from a passed keyword a full db attribute: 
`(concat-keyword :user/countries- :visited)`

```clojure
(defn concat-keyword [part-1 part-2]
  (let [name-1 (str/replace part-1 #"^:" "")
        name-2 (name part-2)]
    (-> (str name-1 name-2)
        (keyword))))
```

And here are the remove and add functions:

```clojure
(defn remove-from-countries [type conn user-email alpha-3]
  "Remove country from list"
  (let [user-id (-> (find-user (d/db conn) user-email)
                    (:db/id))
        country-id (get-country-id-by-alpha-3 (d/db conn) alpha-3)
        attr (concat-keyword :user/countries- type)]
    @(d/transact conn [[:db/retract user-id attr country-id]])))

(defn add-to-countries [type conn user-email alpha-3]
  "Add country to visited list"
  (when-let [country-id (get-country-id-by-alpha-3 (d/db conn) alpha-3)]
    (case type
      :visited  (remove-from-countries :to-visit conn user-email alpha-3)
      :to-visit (remove-from-countries :visited  conn user-email alpha-3))
    (let [attr (concat-keyword :user/countries- type)
          tx-user {:user/email user-email
                   attr        [country-id]}]
      @(d/transact conn [tx-user]))))
```

If we add a country to `visited` list we need to make sure it's been removed from `to-visit` list and vice versa. That's why we need that case statement:

```clojure
(case type
  :visited  (remove-from-countries :to-visit conn user-email alpha-3)
  :to-visit (remove-from-countries :visited  conn user-email alpha-3))
```

And the last thing we need is to get countries by user email:

```clojure
(defn get-countries [db user-email]
  (d/q '[:find (pull ?e
                     [{:user/countries-to-visit
                       [:country/alpha-3]}
                      {:user/countries-visited
                       [:country/alpha-3]}])
         :in $ ?user-email
         :where [?e :user/email ?user-email]]
     db user-email))
```

And now we can test everything together:

```clojure
(add-to-countries :visited conn "test@user.com" "BLR")
(get-countries (d/db conn) "test@user.com")
(add-to-countries :to-visit conn "test@user.com" "BLR")
(get-countries (d/db conn) "test@user.com")
```

Everything should work as expected.

##

In this chapter we learned a new way of running a project directly through REPL, we added a `reset-db` function to `user` namespace, we created a database schema, prepopulated countries data and added some queries.

Code for the end of this chapter can be found in `app/chapter-04/end` folder.




[datamaps]: https://datamaps.github.io/
[datomic-data-model]: https://docs.datomic.com/cloud/whatis/data-model.html
[datomic-schema]: https://docs.datomic.com/cloud/schema/schema-reference.html
[datomic-console-download]: https://my.datomic.com/downloads/console
[datomic-console-docs]: https://docs.datomic.com/on-prem/console.html
[countries-list-github]: https://github.com/lukes/ISO-3166-Countries-with-Regional-Codes
[countries-list-json]: https://raw.githubusercontent.com/lukes/ISO-3166-Countries-with-Regional-Codes/master/slim-3/slim-3.json
[json-to-edn-converter]: http://pschwarz.bicycle.io/json-to-edn/ 
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTE5NjI3Nzg0MzgsLTg4OTEwMTAzOCwxNj
MwODUzNTgwLDEzNTc3OTY3MzIsLTE2MTE2MDY1NywxMDE1NDA1
NjExLDM1NTEwMDAzOF19
-->