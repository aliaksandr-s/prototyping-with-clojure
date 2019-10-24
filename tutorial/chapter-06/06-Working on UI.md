# Working on UI

Our back-end API with registration and authentication is ready so it's time to start working on the front-end part. In this chapter we're planning to add a map and integrate it with out API to make it interactive. But first we need to get some general overview of how everything should work together. So we'll start with a theory block again.

Code for the beginning of this chapter can be found in  `app/chapter-06/start` folder.

## Some theory

To implement our UI we've chosen two main libraries: [reagent] and [re-frame]. We'll also need a library to work with maps but we'll get back to it a bit later. As we already know [reagent] is just a wrapper on top of [react] which abstracts DOM manipulations for us. It helps us to build our UI using a bunch of isolated components --- pretty much like LEGO. It has a simple API and really easy to use. Next picture shows an example of an app composed from such components

<p align="center">
  <img src="https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-06/components.svg?sanitize=true">
</p>

But all those components need some data to show. Sure we can fetch that data directly inside a component and for really small applications it works fine. But when an application starts growing and we have a lot of components and some of them need to share data we're in a big trouble: our code will become really hard to maintain. That's why it's a good idea to keep our components and app logic separated. 

And [re-frame] should help us with solving those problems. It gives us a great structure where we can put everything related to business logic of our app. It gives us a centralized store where we put all the data related to our app. We can change the data in store using events and effects. And we also can subscribe to those data changes in store, which will also cause our UI to automatically update.

Let's have a look at the next diagram and go through all the steps one by one.

![reframe-img]

We have an application that has a button and an empty table, when user clicks the button we'll get some data from the server and populate the table. 

1. When user clicks the button we dispatch an event which notifies the system that we want to fetch some data.
2. To handle this event we should register an event handler. It will declaratively describe what effects should happen to fetch the data and what to do with the response.
3. We also need to register a few more event handlers that will describe how to update our app db if we get data successfully or we got some errors.
4. By this moment our app db supposed to have all data that we need. So it's time to add a subscription that knows how to get that data.
5. And the last step is to add that subscription to a UI component. In our case it's table. When the data we subscribed to changes, our component UI will be updated automatically. 

That was just a general overview, we'll get into more details when we start working with code.

## Adding a map

Now it's time to add a map to our application. But let's run our project first. Here are all the steps:

1. Run datomic
   - `cd {datomic-folder}`
   - `bin/transactor config/samples/free-transactor-template.properties`
   - *Optional gui console*: `bin/console -p 8080 dev datomic:free://localhost:4334`

2. Start a web server:
   - `lein repl`
   - `(start)`

3. Start client:
   - `lein figwheel`
   - Go to `localhost:3000` in your browser

To show a map we planned to use [datamaps]. But after playing around with it for a bit it occurred their map isn't responsive and has no support for zooming out of the box. So it's been decided to use [amcharts] instead. They support much more features out of the box and have a free license if we don't mind (of course we don't) a small amCharts attribution on charts. 

The first step is to install the library. We'll do this using a CDN version. All we need to do is to add the next scripts to the end of `visitera/resources/html/home.html` file

```html
<script src="https://www.amcharts.com/lib/4/core.js"></script>
<script src="https://www.amcharts.com/lib/4/maps.js"></script>
<script src="https://www.amcharts.com/lib/4/geodata/worldLow.js"></script>
```

We can also remove welcome message from that file so it should look like that:

```html
<html>
	<head>
		<meta charset="UTF-8" />
		<meta name="viewport" content="width=device-width, initial-scale=1" />
		<title>Welcome to visitera</title>
	</head>
	<body>
		<div id="app"></div>

		<!-- scripts and styles -->
		{% style "/assets/bulma/css/bulma.min.css" %} 
		{% style "/assets/material-icons/css/material-icons.min.css" %}    
		{% style "/css/screen.css" %}

		<script type="text/javascript">
			var csrfToken = '{{csrf-token}}';
		</script>

		<script src="https://www.amcharts.com/lib/4/core.js"></script>
		<script src="https://www.amcharts.com/lib/4/maps.js"></script>
		<script src="https://www.amcharts.com/lib/4/geodata/worldLow.js"></script>

		{% script "/js/app.js" %}
	</body>
</html>
```

Now we can use [this example][map-example] to add a map to our project. There is just one problem with it: it's written in javascript and it may be hard to convert it to clojure manually. But luckily there is a [transpiler from js to cljs][js-cljs-transpiler].

Here is how our map component should look like after transpilation and some additions:

```clojure
(defn map-component
  []
  (let [create (fn [this]
                 ; Define globals
                 (def am4core (.-am4core js/window))
                 (def am4maps (.-am4maps js/window))
                 (def am4geodata_worldLow (.-am4geodata_worldLow js/window))

                 ; Create map instance
                 (def chart (.create am4core "chartdiv" (.-MapChart am4maps)))

                 ; Set map definition
                 (set! (.-geodata chart) am4geodata_worldLow)

                 ; Set projection
                 (set! (.-projection chart) (new (.-Miller (.-projections am4maps))))

                 ; Create map polygon series
                 (def polygonSeries (.push (.-series chart) (new (.-MapPolygonSeries am4maps))))

                 ; Make map load polygon (like country names) data from GeoJSON
                 (set! (.-useGeodata polygonSeries) true)

                 ; Configure series
                 (def polygonTemplate (.. polygonSeries -mapPolygons -template))
                 (set! (.-tooltipText polygonTemplate) "{name}")
                 (set! (.-fill polygonTemplate) (.color am4core "#74B266"))

                 ; Remove Antarctica
                 (set! (.-exclude polygonSeries) #js ["AQ"])

                 ; add some data
                 (def testData
                   #js
                    [#js {:id    "US"
                          :value 100
                          :fill  (.color am4core "#F05C5C")}
                     #js {:id    "FR"
                          :value 50
                          :fill  (.color am4core "#5C5CFF")}])
                 (set! (.-data polygonSeries) testData)

                 ; Bind "fill" property to "fill" key in data
                 (set! (.. polygonTemplate -propertyFields -fill) "fill"))]

    (r/create-class
     {:display-name  "map-component"
      :reagent-render (fn []
                        [:div {:id "chartdiv"
                               :style {:width "100%"
                                       :height "calc(100vh - 5rem)"}}])
      :component-did-mount
      create})))
```

We put all the logic inside `create` function which will be called after our component is mounted to the DOM. To quickly test everything will put it into `visiteta/src/cljs/visitera/core.cljs` file. And put it inside `home-page` component.

```clojure
(defn home-page []
  [map-component])
```

After saving a file and going to the main screen in the browser we should see a map with a few countries colorized. Great! Now we need to connect it to our back-end. But there is one problem: in our back-end we used *alpha-3* - *(USA, CAN, BLR)* codes for countries but our map expects data in *alpha-2*  - *(US, CA, BY)* format. So we should fix that first.

## Back-end fixes

First we go to [github repo with countries list][countries-list-github] and copy the content of [full countries list][countries-list-json]. Then convert it from JSON to EDN using [json to edn converter][json-to-edn-converter]. Next we'll save everything to `visitera/resources/raw/data.edn`. 

We don't really need all that data about each country so let's do some transformations. So let's create a script that will parse that data and leave only those fields we are interested in. We need only `:name` and `:alpha-2` but we'll also leave `:alpha-3` and `:code` just in case and because we already have them in our schema.

So here is the script that we can place into `visitera/resources/raw/transform-data.edn`:

```clojure
(defn get-raw-data [] (->
                       (slurp "./resources/raw/data.edn")
                       (read-string)
                       (eval)))

(def keys [:name :country-code :alpha-2 :alpha-3])
(def new-keys {:name         :country/name
               :country-code :country/code
               :alpha-2      :country/alpha-2
               :alpha-3      :country/alpha-3})

(defn transform [country]
  (-> country
      (select-keys keys)
      (clojure.set/rename-keys new-keys)))

(defn wrap-with-template [data]
  (str {:visitera/data1 {:txes [(vec data)]}}))

(defn save-parsed []
  (spit "./resources/raw/parsed-data.edn"
        (binding [*print-namespace-maps* false]
          (->>
           (get-raw-data)
           (map transform)
           (wrap-with-template)))))

(save-parsed)
```

Nothing complicated here: just load the content, do some transformations and save it to `visitera/resources/raw/parsed-data.edn`. And to execute it we just connect to our running REPL with our code editor and evaluate the content of the file.

Now let's copy paste `parsed-data.edn` file to `visitera/resources/migrations` and change its name to `countries-data.edn`. We also should do some updates to `visitera/resources/migrations/schema.edn` file.

First add the next field to our country schema:

```clojure
{:db/doc                "Country ISO alpha-2 code"
 :db/ident              :country/alpha-2
 :db/valueType          :db.type/string
 :db/cardinality        :db.cardinality/one
 :db/unique             :db.unique/identity}
```

Second remove all the data about countries and test data. We'll have them in separated files. So our `schema.edn` should contain nothing else but country and user schema.

And let's also put some test data to `visitera/resources/migrations/test-data.edn` file

```clojure
{:visitera/data2
 {:txes
  [[{:user/email
     "test@user.com"
     :user/password
     ; somepass
     "bcrypt+sha512$c0d6f8f472f9312d1ac5cb84b39c858e$12$72eb4c3d6d0f6148c66657da865705f67c1914ef1d66fd2a"
     :user/countries-to-visit
     [{:country/name "Zambia"}
      {:country/name "France"}
      {:country/name "Albania"}
      {:country/name "Andorra"}]
     :user/countries-visited
     [{:country/alpha-2 "RU"}
      {:country/alpha-2 "CZ"}
      {:country/alpha-2 "US"}]}

    {:user/email
     "test@user-1.com"
     :user/password
     ; somepass
     "bcrypt+sha512$c0d6f8f472f9312d1ac5cb84b39c858e$12$72eb4c3d6d0f6148c66657da865705f67c1914ef1d66fd2a"}]]}}
```

And we also need to add some changes to `visitera/src/clj/visitera/db/core.clj`. First let's update `install-schema` function so it would use all the files from `migrations` folder:

```clojure
(def db-resources
  ["migrations/schema.edn"
   "migrations/countries-data.edn"
   "migrations/test-data.edn"])

(defn install-schema
  [conn]
  (for [resource db-resources]
    (let [norms-map (c/read-resource resource)]
      (c/ensure-conforms conn norms-map (keys norms-map)))))
```

Next we need to replace all occurrences of `alpha-3` word to `alpha-2`. And to apply all the changes we did, we need to execute `(reset-db)` function which is located in `user` namespace.

To test that everything worked we need to reevaluate `get-countries` function from `visitera.db.core` namespace and call `(get-countries (d/db conn) "test@user.com")`. As a result we should get something like that:

```clojure
[[#:user
{:countries-to-visit 
 [#:country{:alpha-2 "AL"}
  #:country{:alpha-2 "AD"} 
  #:country{:alpha-2 "FR"}
  #:country{:alpha-2 "ZM"}], 
:countries-visited 
[#:country{:alpha-2 "CZ"} 
 #:country{:alpha-2 "RU"} 
 #:country{:alpha-2 "US"}]}]]
```

That is exactly what we have in our `test-data.edn` file. So that means everything works as expected.

Let's also extend session life so it would be easier to work with the client side. In `visitera.middleware` namespace in `wrap-base` function we just need to replace `(ttl-memory-store (* 60 30))` with `(ttl-memory-store three-days)` and create a private variable `three-days`: 

`(def ^:private three-days (* 60 60 24 3))`

## Showing data on the map

We updated our schema so now we are almost ready to show data on the map. But before we can get back to the front-end part we need to do a few more changes on the back-end side. 

Our `get-countries` function returns too much data. Everything we really need is two lists with *alpha-2* codes of countries user already visited or will visit in future. Something like that: 

```clojure
{:visited ("CZ" "RU" "US"), 
 :to-visit ("AL" "AD" "CN" "FR" "ZM")}
```
So let's add a transformation function and update `get-countries` function:

```clojure
(defn- format-countries [countries]
  (let [countries-content (-> countries (first) (first))
        map-fn (fn [el] (:country/alpha-2 el))]
    {:visited  (map map-fn (:user/countries-visited countries-content))
     :to-visit (map map-fn (:user/countries-to-visit countries-content))}))

(defn get-countries [db user-email]
  (-> (d/q '[:find (pull ?e
                         [{:user/countries-to-visit
                           [:country/alpha-2]}
                          {:user/countries-visited
                           [:country/alpha-2]}])
             :in $ ?user-email
             :where [?e :user/email ?user-email]]
           db user-email)
      (format-countries)))
```
Now if we evaluate everything and try to execute `(get-countries (d/db conn) "test@user.com")` we should get the expected result.

And now we can connect everything together by adding a new route. Let's open `visitera/src/clj/visitera/routes/home.clj` file and add a new handler:

```clojure
(defn get-user-countries-handler [{:keys [session]}]
  (let [email (:identity session)]
    (-> (response/ok (pr-str (get-countries (d/db conn) email)))
        (response/header "Content-Type" "application/edn"))))
```
Here we just get a user email from a `session` and return countries based on that email.

Next we add a new route to `home-routes` function:

```clojure
...
["/api"
    {:middleware [middleware/wrap-restricted]}
    ["/user-countries"
     ["" {:get get-user-countries-handler}]]]
...
```
And we should not forget to import `get-countries` function from `visitera.db.core` namespace

```clojure
(ns visitera.routes.home
  (:require
	...
   [visitera.db.core :refer [conn find-user add-user get-countries]]
	...
```

And we're done with the back-end part for this task and can get back to the client side to focus on actually showing those countries on the map.

Let's open `visitera/src/cljs/visitera/core.cljs` file. And inside `init!` function we replace   `(rf/dispatch [:fetch-docs])` with `(rf/dispatch [:fetch-user-countries])`. The first step is done, we've just dispatched an event. If we go to the main screen in our browser and open console we should see an error like that:

```js
re-frame: no :event handler registered for: 
:fetch-user-countries
```

Let's fix that and add an event handler to `visitera/src/cljs/visitera/events.cljs` file

```clojure
(rf/reg-event-fx
 :fetch-user-countries
 (fn [_ _]
   {:http-xhrio {:method          :get
                 :uri             "/api/user-countries"
                 :response-format (ajax-edn/edn-response-format)
                 :on-success      [:set-countries]
                 :on-failure      [:common/set-error]}}))
```

Here we describe how to get countries and what to do if the request is successful or if there was an error. We pass data from server using EDN so we need explicitly import some helpers to work with it.

```clojure
(ns visitera.events
  (:require
   ...
   [ajax.edn :as ajax-edn]))
```

And we also need to register an event handler that will update our app db when we successfully fetched countries data.

```clojure
(rf/reg-event-db
 :set-countries
 (fn [db [_ countries]]
   (assoc db :countries countries)))
```

Event handler for errors is already there, but we don't really care about handling errors for now.

Now we are ready to test if we can get data from the sever. Let's go to the main screen of our app using these credentials from `test-data.edn`:

```
email:    test@user.com
password: somepass
```
There is should be an icon at the bottom right of the screen to open [re-frisk dev tools][re-frisk]. We can see that `:fetch-user-countries` and `:set-countries` events fired as we expected. But 
countries lists are empty:

```
+ :countries {2 keys}
    :visited (0 items)
    :to-visit (0 items)
```

There must be a bug somewhere, let's try to find it. Our client-side code works as expected and as we remember our database part also returns the right data. But for some reason we get empty lists. So there may be something wrong with our back-end route handlers. So `visitera.routes.home` namespace may be a good place to start our researches.

Let's add a few `println` statements to `get-user-countries-handerler`:

```
(defn get-user-countries-handler [{:keys [session]}]
  (let [email (:identity session)]
    (println email)
    (println (get-countries (d/db conn) email))
    (-> (response/ok (pr-str (get-countries (d/db conn) email)))
        (response/header "Content-Type" "application/edn"))))
```

Now if we restart our browser we should see in the terminal:

```
user=> :test@user.com
{:visited (), :to-visit ()}
```

It seems our email is saved as a keyword in session but we need it to be just a string to get data from the database. So let's fix that. Inside `login-handler` we need to replace:

```clojure
[updated-session (assoc session :identity (keyword (:email params)))]
```

with:

```clojure
[updated-session (assoc session :identity (:email params))]
```

To test everything we need to logout, login again and go to the main page. If we open [re-frisk dev tools][re-frisk] we should see that our countries properly updated:

```
+ :countries {2 keys}
    + :visited (3 items)
        "CZ"
        "RU"
        "US"
    + :to-visit (4 items)
        "AL"
        "AD"
        "FR"
        "ZM"
``` 

We fixed our problem so we can get back to the main task of showing countries on the map. We already created events and event handlers with effects so the next step it to add subscriptions to `visitera.events` namespace. Here it is:

```clojure
(rf/reg-sub
 :countries
 (fn [db _]
   (:countries db)))
```

Now we can use that subscription to get countries data in our `map-component`. But here we have another problem: our `map-component` expects countries to be a list where each country is a map with `:id` and `:fill` properties. 

So we need to transform this:

```clojure
{:visited ("CZ" "RU" "US"), 
 :to-visit ("AL" "AD" "CN" "FR" "ZM")}
```

into this:

```clojure
[{:id     "CZ"
  :status :visited
  :fill   #some-color
}, {..}, {..}, ....]
```

Let's start with creating a `visitera/src/cljs/visitera/config.cljs` and putting some colors there:

```clojure
(ns visitera.config)

(def colors
  {:to-visit    "#f0dd92"
   :visited     "#83b582"
   :not-visited "#dddddd"})
```

Now we can import it in `visitera.events` namespace:

```clojure
(ns visitera.events
  (:require
   ...
   [visitera.config :as cfg]))
```
And implement a transformation function:

```clojure
(defn- normalize-countries [countries]
  (into [] cat [(->> (:visited countries)
                     (map (fn [c-id] {:id     c-id
                                      :fill   (:visited cfg/colors)
                                      :status :visited})))
                (->> (:to-visit countries)
                     (map (fn [c-id] {:id     c-id
                                      :fill   (:to-visit cfg/colors)
                                      :status :to-visit})))]))
```

And now we can use a great feature of [re-frame] and chain a few subscriptions together:

```clojure
(rf/reg-sub
 :normalized-countries
 (fn []
   (rf/subscribe [:countries]))
 (fn [countries]
   (normalize-countries countries)))
``` 
So any time `:counties` gets updated we also recalculate `:normalized-countries` using our transformation function.

And now we are ready to connect countries data with the map. But let's do some refactoring first to make it easier working with `map-component`. Let's create a `visitera/src/cljs/visitera/components` folder and a `map.cljs` file inside it. Now we can move `map-component` from `visitera.core` namespace to a newly created file to a `visitera.components.map` namespace.

```clojure
(ns visitera.components.map
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [visitera.config :as cfg]))

(defn map-component
  []
  (let [create (fn [this]
  ...
```

And we should not forget to `require` it inside `visitera.core` namespace

```clojure
(ns visitera.core
  (:require
	...
   [visitera.components.map :refer [map-component]]
	...
```
And we update our `home-page` function like that:

```clojure
(defn home-page []
  [:div.content
   [map-component]])
```

Now we can work on our map in a separated file. And if we update the main page nothing should break.

Let's start with adding a wrapper to `visitera.components.map` namespace where we add our subscriptions

```clojure
(defn map-component []
  (let [norm-countries (rf/subscribe [:normalized-countries])
        countries      (rf/subscribe [:countries])]
    (fn []
      (if @countries
        [:div
         [:div [map-component-inner @norm-countries]]]
        [:div "Loading"]))))
```
We called it `map-component` so we need to change the name of our original `map-component` to `map-component-inner`

We pass countries as a property to `map-component-inner`

```clojure
(defn map-component-inner
  [countries]
  (let [create (fn [this]
  ...
```

And now we can remove test data that we had and replace it with real countries.

```clojure
(set! (.-data polygonSeries) (clj->js countries))
```
Our map expects data in `js` format so we also had to convert `clj` to `js`.

And that's it. If we go to the browser we should see that data from the sever is reflected on the map now.

## UI improvements

Before we start working on map updates let's improve our UI a bit. It would be nice to have a legend that shows the amount of each type of countries, buttons for scaling the map would also be a great feature, and navbar definitely should be updated.

### Scaling the map

Let's start with adding buttons to zoom in and zoom out our map. That's actually pretty simple: all we need to do is to add one line to `map-inner-component`

```clojure
...
; Set projection
(set! (.-projection chart) (new (.-Miller (.-projections am4maps))))

; Add zoom control
(set! (.-zoomControl chart) (new (.-ZoomControl am4maps)))
...
```

Now we should see zoom controls at the bottom left corner of the map.

### Updating the navbar

Let's make a few updates to our navbar too. It's missing logout link, color can be changed, and all the other links can be removed. 

Let's first create a separate file for it inside `visitera/src/cljs/visitera/components/navbar.cljs`.

And put that code inside:

```clojure
(ns visitera.components.navbar
  (:require
   [reagent.core :as r]))

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-primary>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "visitera"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click #(swap! expanded? not)
        :class (when @expanded? :is-active)}
       [:span] [:span] [:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-end
       [:a.navbar-item {:href "/logout"} "Logout"]]]]))
``` 

And then we just require it inside `visitera.core` namespace. 

```clojure
...
[visitera.components.navbar :refer [navbar]]
...
```
And remove the old code.

We can also remove everything related to the `about-page` because we'll have only one page with the map.

### Adding a legend

Now we have a nice navbar with a logout link. So it's time to add a legend which will show show the amount of countries we visited or planning to visit. 

First we need to add a few subscriptions that will count the amount of countries to `visitera.events` namespace:

```clojure
(rf/reg-sub
 :visited-count
 (fn [db _]
   (-> db :countries :visited count)))

(rf/reg-sub
 :to-visit-count
 (fn [db _]
   (-> db :countries :to-visit count)))
```

Then we create a legend component inside `visitera.components.map` namespace

```clojure
(defn legend-tag
  [color]
  [:span
   {:style {:display          "inline-block"
            :width            "1.3rem"
            :height           "1.3rem"
            :border-radius    "5px"
            :background-color color}}])

(defn legend-row
  [color text count]
  [:div
   {:style {:display               "grid"
            :grid-template-columns "1.7rem 7rem 1.7rem"}}
   [legend-tag color]
   [:span text]
   [:span count]])

(defn legend-comp
  [to-visit-count visited-count]
  [:div
   {:style {:position "fixed"
            :top      "60%"
            :left     "5%"}}
   [legend-row
    (cfg/colors :to-visit)
    "Want to visit: "
    to-visit-count]
   [legend-row
    (cfg/colors :visited)
    "Already visited: "
    visited-count]])
```

We also created `legend-tag` and `legend-row` as helper components.

And as a last step we need to connect our subscriptions with legend map component

```clojure
...
(defn map-component []
  (let [norm-countries (rf/subscribe [:normalized-countries])
        countries      (rf/subscribe [:countries])
        visited-count  (rf/subscribe [:visited-count])
        to-visit-count (rf/subscribe [:to-visit-count])]
    (fn []
      (if @countries
        [:div
         [:div [map-component-inner @norm-countries]]
         [:div [legend-comp @to-visit-count @visited-count]]]
        [:div "Loading"]))))
```

And now we can go to the browser and make sure that our UI looks much better.

## Map interactivity

Our app is almost ready. There is only one last step and the most important feature of our app --- map interactivity. When we click on a country on the map it should change its state, giving us an ability to visually track our trips.

As usual let's start with the back-end part. Here is a handler that we need to add to `visitera.routes.home` namespace:

```clojure
(defn put-user-countries-handler [{:keys [params session]}]
  (let [email (:identity session)
        status (:status params)
        country (:id params)]
    (try
      (update-countries conn email status country)
      (-> (response/ok (pr-str (get-countries (d/db conn) email)))
          (response/header "Content-Type" "application/edn"))
      (catch Exception e (response/bad-request
                          (str "Error: " (.getMessage e)))))))
``` 

We just get status and country id form a user and try to update our database, if everything is okay we return updated countries, and in case of an error we send back an error message.

And we also need to add that handler to `home-routes` list:

```clojure
...
["/user-countries"
     ["" {:get get-user-countries-handler
          :put put-user-countries-handler}]]
```

Now we can get back to front-end and add event handlers to `visitera.events` namespace. `:update-user-countries` to handle requests to the server. And `:set-last-updated` to do optimistic updates and not to rerender the whole map component.

```clojure
(rf/reg-event-db
 :set-last-updated
 (fn [db [_ country]]
   (assoc db :last-updated country)))

(rf/reg-event-fx
 :update-user-countries
 (fn [{:keys [db]} [_ country]]
   {:http-xhrio {:method          :put
                 :uri             "/api/user-countries"
                 :params          country
                 :format          (ajax-edn/edn-request-format)
                 :response-format (ajax-edn/edn-response-format)
                 :on-success      [:set-countries]
                 :on-failure      [:common/set-error]}
    :dispatch [:set-last-updated country]}))
```
When we click on the map we'll do two actions at the same time: send a request to the server to update data in the database and update our map immediately because we already know how it should be updated.

And to get the last country that was updated we also add a subscription to `visitera.events` namespace.

```clojure
(rf/reg-sub
 :last-updated
 (fn [db _]
   (:last-updated db)))
```


[reagent]: https://reagent-project.github.io/
[re-frame]: https://github.com/Day8/re-frame
[react]: https://reactjs.org/
[components-img]: https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-06/components.svg?sanitize=true
[reframe-img]: https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-06/Re-frame.svg?sanitize=true
[datamaps]: https://datamaps.github.io/
[amcharts]: https://www.amcharts.com/
[map-example]: https://codepen.io/team/amcharts/pen/jzeoay
[js-cljs-transpiler]: https://roman01la.github.io/javascript-to-clojurescript/
[countries-list-github]: https://github.com/lukes/ISO-3166-Countries-with-Regional-Codes
[countries-list-json]: https://raw.githubusercontent.com/lukes/ISO-3166-Countries-with-Regional-Codes/master/all/all.json
[json-to-edn-converter]: http://pschwarz.bicycle.io/json-to-edn/
[re-frisk]: https://github.com/flexsurfer/re-frisk
<!--stackedit_data:
eyJoaXN0b3J5IjpbMTI3MjUzMzc5NSwtMTA3MjIxNDkxNyw4OT
g5NTIxOTgsNDM4NTA2NDM1LDE2ODUwMDQ1NjcsLTE0NjYwNzMy
OTddfQ==
-->