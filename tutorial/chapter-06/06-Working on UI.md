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

                 ; Create hover state and set alternative fill color
                 (def hs (.create (.-states polygonTemplate) "hover"))
                 (set! (.. hs -properties -fill) (.color am4core "#367B25"))

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

## Showing data on a map


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
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTk5MDIyNzc3NywtMTA3MjIxNDkxNyw4OT
g5NTIxOTgsNDM4NTA2NDM1LDE2ODUwMDQ1NjcsLTE0NjYwNzMy
OTddfQ==
-->