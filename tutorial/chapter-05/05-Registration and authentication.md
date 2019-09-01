# Registration and authentication

By now our app has a data layer so we can start working on our backed service. In this chapter we will have a look at some theory behind registration and authentication, implement routes and handlers and add some tests.

Code for the beginning of this chapter can be found in  `app/chapter-05/start` folder.

## Some theory

There are a lot of different ways to implement authentication in modern web applications: session, JWT, oauth2. For our app we'll go with the simplest one: session based authentication. 
 
The authentication process simply consists of checking the `:identity` keyword in session. And session is just an abstraction that holds some data about the client. There are a lot of different ways to store them: in memory, database, cookies. For our app in-memory solution is more that enough. On the client session id is stored in cookies and being sent to the server with each request. Then server will get all the information associated with that id from memory. We'll get into more details when we start working with code.

## Registration

Let's start with registration. There is nothing fancy here: user goes to the `/register` route and gets a form which is rendered on the sever. There are two fields: email and password. User submits a form, we check it on the server: if everything is fine we create a user and redirect to `/login` page, if there are any validation errors we insert them into the form and return back to user. 

Here is a diagram of registration flow that shows all the details:

![registration-diagram]

As the first step we obviously need to start the project:

 1. Go to datomic folder:  `$ cd {datomic-folder}`
 2. Run the transactor: `$ bin/transactor config/samples/free-transactor-template.properties`
 3. Go to project folder: `$ cd {project-folder}`
 4. Start REPL `$ lein repl`
 5. Start the project from REPL `user=> (start)` 

Next we need to create HTML templates. We'll have two forms: one for registration and another one for login. So to prevent copy-pasting we'll create one common HTML template and simply extend it with different forms.

```
  +-------------------+     +-------------------+
  |  auth.html        |     |  auth.html        |
  | +---------------+ |     | +---------------+ |
  | | register.html | |     | | login.html    | |
  | |               | |     | |               | |
  | +---------------+ |     | +---------------+ |
  +-------------------+     +-------------------+
```

Our app template already comes with [bulma css framework][bulma] preinstalled, so we don't need to worry about styling. And all the HTML templates are located in `resources/html` folder.

Here is the content of `auth.html`:

```html
<!DOCTYPE html>
<html>
    <head>
        <title>Visitera</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        {% style "/assets/bulma/css/bulma.min.css" %}
    </head>
    <body>
        <div class="hero is-primary is-fullheight">
            <div class="hero-head">
                <nav class="navbar">
                    <div class="container">
                        <div class="navbar-brand">
                            <a href="/" class="navbar-item has-text-white" style="font-weight: bold;">VisiterA</a>
                        </div>
                    </div>
                </nav>
            </div>
            <div class="hero-body">
                <div class="hero-body">
                    <div class="container">
                        <div class="columns is-centered">
                            <div class="column is-5-tablet is-5-desktop
                                is-4-widescreen">
                                {% block form %}
                                {% endblock %}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
```

In the head we include our css framework. And the next block we'll be replaced with the content of the form.

```html
{% block form %}
{% endblock %}
```

And here is our `register.html` with comments explaining the main parts:

```html
{% extends "auth.html" %}  <!--  here we specify what template to extend  -->
{% block form %}           <!--  a place in auth template where the next code will be injected -->
<form method="POST" action="/register" class="box"> 
    {% csrf-field %}       <!--  more about this here http://www.luminusweb.net/docs/security.html#cross_site_request_forgery_protection -->
    <label class="label is-medium has-text-centered">Create Account</label>
    <div class="field">
        <label for="email" class="label">Email</label>
        <div class="control has-icons-left">
            <!-- Make input red if any errors in validation. 
                 Also pass email back from the server so user wouldn't have to type it again -->
             <input type="text"
                   name="email"
                   placeholder="e.g.bobsmith@gmail.com"
                   class="input {% if errors.email %} is-danger {% endif %}"
                   value="{% if email %}{{email}}{% endif %}"
            />
           <span class="icon is-small is-left">
                <i class="fa fa-envelope"></i>
            </span>
        </div>
        <!-- Show validation errors if any -->
        {% if errors.email %}
        <p class="help is-danger">{{errors.email}}</p>
        {% endif %}

   </div>
    <div class="field">
        <label for="password" class="label">Password</label>
        <div class="control has-icons-left">
            <input type="password"
                   name="password"
                   placeholder="*******"
                   class="input {% if errors.password %} is-danger {% endif %}"
 
            />
            <span class="icon is-small is-left">
                <i class="fa fa-lock"></i>
            </span>
        </div>
        {% if errors.password %}
        <p class="help is-danger">{{errors.password}}</p>
        {% endif %}
    </div>
   <div class="field">
        <button class="button is-success" style="width: 100%">
            Register
        </button>
    </div>
    <div class="field has-text-centered">
        <span>Already a user? <a href="/login">Log in</a></span>
    </div>
</form>
{% endblock %}
```

All the templates are ready so now we can add a function that will handle the rendering. Let's put the next piece of code to `src/clj/visitera/layout.clj` file:

```clojure
(defn register-page [request]
  (render
   request
   "register.html"))
``` 

Let's also move `home-page` function from `visitera.routes.home` namespace to `visitera.layout` namespace.

```clojure
(defn home-page [request]
  (render request "home.html"))
```

And the last part is adding route handler to our `visitera.routes.home` namespace, and removing everything we don't need. Here's how it should look like by now: 

```clojure
(ns visitera.routes.home
  (:require
   [clojure.java.io :as io]
   [visitera.layout :refer [register-page home-page]]
   [visitera.middleware :as middleware]
   [ring.util.http-response :as response]
   [visitera.db.core :refer [conn find-user]]
   [datomic.api :as d]))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]
   ["/register" {:get register-page}]])
```

Now we can go to `http://localhost:3000/register` and have a look at our form. But there are a few issues with it right now: submitting obviously isn't working yet, and icons aren't shown. Let's fix the second one first. 

Icons aren't show because [bulma] uses [font-awesome] as a dependency. It's not coming preinstalled so we need to install it manually. First we go to [webjars] website, choose `Leiningen` as a build tool, and search for `font-awesome`. The first link should be the correct one, just the version may be different. For me it is: `org.webjars/font-awesome "5.9.0"`. Next we just add it to `:dependencies` in our `project.clj` file. 

```clojure
:dependencies [ ...
				[org.webjars/font-awesome "5.9.0"]
				...]
```

Then we can use `$ lein deps` command to install it explicitly and restart our app. Or just restart our app that will install all the dependencies implicitly:

 1. Stop: <kbd>CTRL + D</kbd>
 2. Start REPL: `$ lein repl`
 3. Run application: `=> (start)`

The last part is to add `.css` file to the head of `auth.html` template:

```html
<head>
     <title>Visitera</title>
     <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
     <meta name="viewport" content="width=device-width, initial-scale=1.0" />
     {% style "/assets/bulma/css/bulma.min.css" %}
     {% style "/assets/font-awesome/css/all.css" %}
</head>
```

Now we should be able to see nice icons of a locker and an envelope in our register form.

There are a few more steps to finish our register form: we need to add a route handler and a validation schema for inputs.

Let's add validation logic to the next file `src/cljc/visitera/validation.cljs`

```clojure
(ns visitera.validation
  (:require [struct.core :as st]))

(def register-schema
  [[:email
    st/required
    st/string
    st/email]

   [:password
    st/required
    st/string
    {:message "password must contain at least 8 characters"
     :validate #(> (count %) 7)}]])

(defn validate-register [params]
  (first (st/validate params register-schema)))
```

Because it's located in `cljc` folder it can be shared between client and server. The code by itself is pretty straightforward. We need to check that email and password exist and both are strings, for email we use default email validation, and for password check if it's more than 7 characters.

And the last part is to add handler and update dependencies and routes in `visitera.routes.home` namespace:

```clojure
(ns visitera.routes.home
  (:require
   [clojure.java.io :as io]
   [visitera.layout :refer [register-page home-page]]
   [visitera.middleware :as middleware]
   [ring.util.http-response :as response]
   [visitera.db.core :refer [conn find-user add-user]]
   [visitera.validation :refer [validate-register]]
   [datomic.api :as d]))

(defn register-handler! [{:keys [params]}]
  (if-let [errors (validate-register params)]
    (-> (response/found "/register")
        (assoc :flash {:errors errors 
                       :email (:email params)}))
    (if-not (add-user conn params)
      (-> (response/found "/register")
          (assoc :flash {:errors {:email "User with that email already exists"} 
                         :email (:email params)}))
      (-> (response/found "/login")
          (assoc :flash {:messages {:success "User is registered! You can log in now."} 
                         :email (:email params)})))))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]
   ["/register" {:get register-page
                 :post register-handler!}]])
```

All that code is explained on the diagram in the beginning of a chapter. We submit a form validate input data and try to create a user if user already exists or any errors in validation we return that form back and using flash messages show appropriate errors. 
   
We don't want to save passwords as it is because it's a big security flaw. So we need to add a few small changes to `visitera.db.core` namespace. 

First let's add to dependencies:

```clojure
[buddy.hashers :as hs]
``` 

And update `add-user` function:

```clojure
(defn add-user
  "Adds new user to a database"
  [conn {:keys [email password]}]
  (when-not (find-one-by (d/db conn) :user/email email)
    @(d/transact conn [{:user/email    email
                        :user/password (hs/derive password)}])))
```

And the very very last thing we need to do is to update `register-page` function in `visitera.layout` namespace so we could pass error messages to our html template

```clojure
(defn register-page [{:keys [flash] :as request}]
  (render
   request
   "register.html"
   (select-keys flash [:errors :email])))
```

And we're done with registration part and now can go to `http://localhost:3000/register` to test it. We should see appropriate errors if we try to submit a form with incorrect data, the email should not get lost between requests. There is only one issue that we have here: error messages won't fade away when we start typing again, we'll fix that a bit later. If input data is correct we should be redirected to `http://localhost:3000/login` which we'll implement shortly too.

## Authentication

Registration is ready so it's time to start implementing authentication. The main flow is shown on the next diagram:

![authentication-diagram]

The first part is really similar to registration. We go to `/login` page and submit login form with user credentials. Next we validate form data on the server side. If it's not correct we return the form back with appropriate errors. If it's correct we try to get user data form the database. If there is no such user or password is wrong we return login form with errors back to the client. If everything is correct we add the `:identity` field to the session. Then each request that should be protected will be wrapped with `wrap-restricted` middleware. That middleware just checks if a session has`:identity` field. If it has that field than everything is okay and our request will be passed to the next handler. If there is no `:identity` field we will be redirected back to the `/login` page.

Let's start with the `login.html` template first. It's pretty similar to `register.html` just two inputs a button and a link. And we just added an extra success message that will be shown right after registration.

```clojure
{% extends "auth.html" %}
{% block form %}
<form method="POST" action="/login" class="box">
    {% csrf-field %}
    <label class="label is-medium has-text-centered">Log in</label>
    {% if messages.success %}
      <p class="has-text-success level-item has-text-centered">{{messages.success}}</p>
    {% endif %}
    <div class="field">
        <label for="email" class="label">Email</label>
        <div class="control has-icons-left">
            <input type="text"
                   name="email"
                   placeholder="e.g.bobsmith@gmail.com"
                   class="input {% if errors.email %} is-danger {% endif %}"
                   value="{% if email %}{{email}}{% endif %}"
            />
            <span class="icon is-small is-left">
                <i class="fa fa-envelope"></i>
            </span>
        </div>
        {% if errors.email %}
        <p class="help is-danger">{{errors.email}}</p>
        {% endif %}

   </div>
    <div class="field">
        <label for="password" class="label">Password</label>
        <div class="control has-icons-left">
            <input type="password"
                   name="password"
                   placeholder="*******"
                   class="input {% if errors.password %} is-danger {% endif %}"
 
            />
            <span class="icon is-small is-left">
                <i class="fa fa-lock"></i>
            </span>
        </div>
        {% if errors.password %}
        <p class="help is-danger">{{errors.password}}</p>
        {% endif %}
    </div>
   <div class="field">
        <button class="button is-success" style="width: 100%">
            Login
        </button>
    </div>
    <div class="field has-text-centered">
        <span>Not a user? <a href="/register">Register</a></span>
    </div>
</form>
{% endblock %}
```

And we also need to add a rendering handler to `visitera.layout` namespace:

```clojure
(defn login-page [{:keys [flash] :as request}]
  (render
   request
   "login.html"
   (select-keys flash [:errors :email])))
```

It's almost identical to `register-page` so to avoid repeating we can refactor our code and create a more generic `auth-page`:

```clojure
(defn auth-page [type]
  (fn [{:keys [flash] :as request}]
  (render
   request
   (str type ".html")
   (select-keys flash [:errors :email :messages]))))

(def register-page (auth-page "register"))
(def login-page (auth-page "login"))
```

Now let's add a validation schema to `visitera.validation` namespace:

```clojure
(def login-schema
  [[:email
    st/required
    st/string
    st/email]

   [:password
    st/required
    st/string]])

(defn validate-login [params]
  (first (st/validate params login-schema)))
```

It's pretty similar to a register one, we just don't validate password length.

Now it's time to update `visitera.routes.home` namespace. Here how it should look like:

```clojure
(ns visitera.routes.home
  (:require
   [clojure.java.io :as io]
   [visitera.layout :refer [register-page login-page home-page]]
   [visitera.middleware :as middleware]
   [ring.util.http-response :as response]
   [visitera.db.core :refer [conn find-user add-user]]
   [datomic.api :as d]
   [visitera.validation :refer [validate-register validate-login]]
   [buddy.hashers :as hs]))

(defn register-handler! [{:keys [params]}]
  (if-let [errors (validate-register params)]
    (-> (response/found "/register")
        (assoc :flash {:errors errors 
                       :email (:email params)}))
    (if-not (add-user conn params)
      (-> (response/found "/register")
          (assoc :flash {:errors {:email "User with that email already exists"} 
                         :email (:email params)}))
      (-> (response/found "/login")
          (assoc :flash {:messages {:success "User is registered! You can log in now."} 
                         :email (:email params)})))))

(defn password-valid? [user pass]
  (hs/check pass (:user/password user)))

(defn login-handler [{:keys [params session]}]
  (if-let [errors (validate-login params)]
    (-> (response/found "/login")
        (assoc :flash {:errors errors 
                       :email (:email params)}))
    (let [user (find-user (d/db conn) (:email params))]
      (cond
        (not user)
        (-> (response/found "/login")
            (assoc :flash {:errors {:email "user with that email does not exist"} 
                           :email (:email params)}))
        (and user
             (not (password-valid? user (:password params))))
        (-> (response/found "/login")
            (assoc :flash {:errors {:password "The password is wrong"} 
                           :email (:email params)}))
        (and user
             (password-valid? user (:password params)))
        (let [updated-session (assoc session :identity (keyword (:email params)))]
          (-> (response/found "/")
              (assoc :session updated-session)))))))

(defn logout-handler [request]
  (-> (response/found "/login")
      (assoc :session {})))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page
         :middleware [middleware/wrap-restricted]}]
   ["/docs" {:get (fn [_]
                    (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                        (response/header "Content-Type" "text/plain; charset=utf-8")))}]
   ["/register" {:get register-page
                 :post register-handler!}]
   ["/login" {:get login-page
              :post login-handler}]
   ["/logout" {:get logout-handler}]])
```

The code is pretty straightforward. If input data is correct and user exists we update a session. If there are any validation errors or user doesn't exist we return the form back with errors. And on logout we just clean a session. 

There is just one tiny update we need to make in `find-user` function in `visitera.db.core` namespace to prevent errors when user doesn't exist:

```clojure
(defn find-user [db email]
  "Find user by email"
  (if-let [user-id (find-one-by db :user/email email)]
    (d/touch user-id)))
```

Now let's test everything. Let's go to `/register` route in our browser. First let's try to submit an empty form. We should immediately see validation errors but they won't fade away if we start typing, so let's fix that. Let's add that script after closing `</body>` tag to our `auth.html` file:

```html
<script type="text/javascript">
      (function() {
        const inputs = document.querySelectorAll('.input');
        const hideErrors = (input) => {
          try {
            input.classList.remove('is-danger')
            input.parentNode.parentNode.children[2].style.display = "none"
          } catch(error) {
            undefined
          }
       }
        inputs.forEach(input => input.addEventListener('focus', () => hideErrors(input)))
      })();
    </script>

```

It just finds error elements in the DOM and removes them when we set focus on inputs.

Let's refresh our app in a browser and try to register a random user. If everything went smoothly we should be redirected to `/login` page and see a success message. Now let's try to visit our main route `/`. We should get an error: `Access to / is not authorized`. That's the correct behavior but our users wouldn't be so happy to see that message so let's better redirect them to `/login` page instead. 

Here are a few changes we need to make in `visitera.midleware` namespace:

First require:

```clojure
[ring.util.http-response :as response]
```

Second update `on-error` function:

```clojure
(defn on-error [request response]
  (response/found "/login"))
```

And now if we go to `/` we should be redirected to `/login`. That's much better. 

Now let's try to login with a previously created user. If everything went okay we should be able to see the main page. 

And it's time to go to `/logout` route. It should destroy current session and redirect us back to `/login`. And our authentication is done.


[registration-diagram]: https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-05/Registration%20Flow.svg?sanitize=true
[authentication-diagram]: https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-05/Authentication%20Flow.svg?sanitize=true
[bulma]: https://bulma.io/documentation/
[font-awesome]: https://fontawesome.com/
[webjars]: https://www.webjars.org/
<!--stackedit_data:
eyJoaXN0b3J5IjpbMTk4OTIxMTM4NCwtMTQzMjA4NDk1MCwtNz
MyODc4MTQ3LDIwNzgxNTgzODgsLTI4Mjk1NTI0MSwtMTAwMDY5
MDE4OCwyMDc4Njc3Nzc2LDY0MjQzMjg3OF19
-->