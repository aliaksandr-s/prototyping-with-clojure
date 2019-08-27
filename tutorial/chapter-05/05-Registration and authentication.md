# Registration and authentication

By now our app has a data layer so we can start working on our backed service. In this chapter we will have a look at some theory behind registration and authentication, implement routes and handlers and add some tests.

Code for the beginning of this chapter can be found in  `app/chapter-05/start` folder.

## Some theory

There are a lot of different ways to implement authentication in modern web applications: session, JWT, oauth2. For our app we'll go with the simplest one: session based authentication. 

The authentication process simply consists of checking the `:identity` keyword in session. And session is just a tiny piece of encoded data that being sent with every request from the client. On the client it's stored in cookies and its value will look something like that: `3be2b6e9-0973-4860-b97f-a0f143cc1d8a`. On the server it will be decoded and we can get more sense out of it:  `{:identity :test@user.com}`. We'll get into more details when we start working with code.

## Registration

Let's start with registration. There is nothing fancy here: user goes to the `/register` route and gets a form which is rendered on the sever. Then user submits a form, we check it on the server: if everything is fine we create a user and redirect to `/login` page, if there are any validation errors we insert them into the form and return back to user. 

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

In the head we include our css framework. And this block we'll be replaced with the content of the form.

```html
{% block form %}
{% endblock %}
```





## Authentication

![authentication-diagram]



[registration-diagram]: https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-05/Registration%20Flow.svg?sanitize=true
[authentication-diagram]: https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-05/Authentication%20Flow.svg?sanitize=true
[bulma]: https://bulma.io/documentation/
<!--stackedit_data:
eyJoaXN0b3J5IjpbMjc4MTM3NzQsLTEwMDA2OTAxODgsMjA3OD
Y3Nzc3Niw2NDI0MzI4NzhdfQ==
-->