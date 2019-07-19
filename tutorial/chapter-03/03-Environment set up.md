# Environment set up

In this chapter we'll do all the necessary preparations before we can actually start coding. We'll install dependencies, scaffold the project, do some REPLing, set up our code editor.

## Installing dependencies

Clojure is a hosted language and it's beign compiled to JVM bytecode and then beign executed by JVM. And to work with it we should have `java` installed (for now it's recommended to use `java 8`). Then we need something for managing dependencies, handling versioning, and building our projects. And `Leiningen` should help us here. And the last thing we need to install is `Datomic`.

Here are the links where you can get everything:

 - [Java 8][java-download]
 - [Leiningen][lein-install]
 - [Datomic][datomic-download]

## Starting the project

Now that we have everything installed let's create a new project and verify that everything works. But first we need to run a database. Follow these steps to launch it:

1. Go to the folder where you installed it. 
2. Run that command: 

```bash
$ bin/transactor config/samples/free-transactor-template.properties
```

If everything is okay you should see something like that:

```bash
System started datomic:free://localhost:4334/<DB-NAME>
```

Now that we have a database running we can start our project. But I just realized that we don't have a name for it. Sure we can call it `myapp`, `testapp`, just `app` it actually doesn't matter but that would be too boring. So let's choose something more beautiful and meaningful. For example `visitera` sounds like a good name for me.  Now lets run the following command in a new terminal window:

```bash
$ lein new luminus visitera +datomic +re-frame +auth
```

It will scaffold a project for us with all the dependencies we need. Code for the project after this step will be in folder `app/chapter-3/start`.

Now lets go to a newly created directory and run the project. 

```bash
$ cd visitera
$ lein run
```

If everything went okay we should see these lines in the terminal:

```bash
server started on port 3000
starting nREPL server on port 7000
```

Let's go to `http://localhost:3000/` in our browser to see what we get.
We should see something like that:

```
Welcome to visitera

If you're seeing this message, that means you haven't yet compiled your ClojureScript!

Please run  lein figwheel  to start the ClojureScript compiler and reload the page.
```

We can see a response, that means our backend part is working. And that message says that we need to compile our clientside code to make it work. Let's run the suggestet command `lein figwheel` in a new terminal window and see what will happen. If everything went fine after refreshing the browser we should see a different page with that phrase:

```
Congratulations, your Luminus site is ready!
```







 Now let's try to use REPL. We know that it's running on port `7000` we just need to connect to it using this command:

`$ lein repl :connect localhost:7000`

And we should see its prompt sign `user=>`. And now we can interract with our running programm. First let's enter to the namespace with our route handler which is located in that file `/src/clj/my_app/handler.clj`.

`=> (in-ns 'my-app.handler)`

And now we can run the next command which should return the same html we just saw earlier in our browser.

`=> (app-routes {:request-method :get, :uri "/"})`

That's pretty cool but we can also interract with our app inside the browser using cljs REPL. First we need to run that command:

`$ lein figwheel`

It will compile our Clojurescript to javascript and will run a browser REPL on port `7002`. Our terminal prompt should change to `app:cljs.user=>` and now we can interract with REPL in the same terminal window. Also if we go to the browser again we should see a different content on the main page because it's been replaced by cljs script which is located here: `/src/cljs/my_
app/core.cljs`.  Now Let's evaluate those expressions:

```clj
=> (.-innerHTML (.getElementById js/document "app"))
=> (set! (.-innerHTML (js/document.getElementById "app")) "Hello world!")
```

 You should immediatelly see the result in your browser.  That shouldn't be so hard to understand what that code does. But if you're confused `.` is used to call methods on js objects and `.-` is for accessing properties. 

## Setting up a text editor.

Using REPL gives us a really cool interactive development experience. But we can make it even better by integrating REPL to a text editor which will give us an extremely pleasurable development experience. Here I'll show how to do this using [VSCode][3].  Basically all we need is to install [Calva][4] extension. Now when we open our project in VSCode we should see an input on the top. If there is no input we can press <kbd>CTRL</kbd>+<kbd>Shift</kbd>+<kbd>P</kbd> to get a command input and type in 

`> Calva: Connect to a running REPL`. 

Then choose `Figwhell` and specify adress with a port `localhost:7002`. And let's test it now. Open `/src/cljs/my_app/core.cljs` and change a string `"Welcome to my-app"` to `"Hello clojure!"`.  Now without saving anything put your cursor before the opening or after closing bracket of the `mount-components` function and run the command:

`> Calva: evaluate current form/selection inline and print to output`

And now we need to run this function the same way. Put your cursor before or after `(mount-components)` expression inside `init!` function and eval it using Calva. Text in your browser should immediatelly change. And of course I'd recommend using hotkeys to evaluate expressions using Calva. 

Code for this chapter can be found in `app/chapter-2` folder.







[lein-install]: https://leiningen.org/#install
[java-download]: https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[datomic-download]: https://my.datomic.com/downloads/free
[3]: https://code.visualstudio.com/
[4]: https://github.com/BetterThanTomorrow/calva
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTUyMjg0MDQ5OSwzMTk2MDg2MCwxMDc1ND
c2ODU2LDQ1OTY0ODQ5XX0=
-->