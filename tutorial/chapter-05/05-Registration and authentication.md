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

Recruiters and companies don’t really like when a candidate turns down a job offer after accepting it. It’s just unprofessional. But sometimes companies do the same thing, and change their mind after making an offer. That happened to me and the company didn’t even bother to notify me. 

That was a bit of an introduction. But the main idea is that now I’m open for new opportunities. 

What I’m looking for:
- Clojure/ClojureScript tech stack
- A friendly team of professional engineers 

What you can expect from me:
- about 6 months working with Clojure 
- 3+ years working with JavaScript (mostly with React and Node)
- great attention to detail and clean code
- love for good UX and desire to learn new stuff

Sharing is really welcome. Thanks!

## Authentication

![authentication-diagram]



[registration-diagram]: https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-05/Registration%20Flow.svg?sanitize=true
[authentication-diagram]: https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-05/Authentication%20Flow.svg?sanitize=true
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTU2Mjg5MDc2MiwtMTAwMDY5MDE4OCwyMD
c4Njc3Nzc2LDY0MjQzMjg3OF19
-->