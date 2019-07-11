# Environment set up

In this chapter we'll do all the necessary preparations before we can actually start coding. We'll install dependencies, scaffold the project, do some REPLing, set up our code editor.

## Installing dependencies

To get started we need only `java` and `Leiningen`. For now it's recommended to use `java 8`.  To install it in Ubuntu we can simply use this command: 

`$ sudo apt-get install openjdk-8-jre`. 

To verify that everything worked:

`$ java -version`

To install Leiningen I suggest using [instructions from their website][1].
That's actually it. But I should probably say a few words about what we just installed and why. Clojure is a hosted language and it's beign compiled to JVM bytecode and then beign executed by JVM that's why we need to install java. Leiningen is used to manage dependencies, bundle and scaffold your projects. If you're comming from javascript world it's like npm, webpack, gulp... and any other tools you might think at once.

## Trying REPL

Now that we have everything installed we need to verify that everything works. We don't want to spend too much time for setting up the project so we'll use [Luminus framework][2] for scaffolding. Let's run those commands:

`$ lein new luminus my-app +cljs`
`$ cd my-app`
`$ lein run`

We should see these lines in terminal:

`server started on port 3000`
`starting nREPL server on port 7000`

Go to `http://localhost:3000/` and to see that everything works. Now let's try to use REPL. We know that it's running on port `7000` we just need to connect to it using this command:

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

Code for this chapter can be found in `app/chapter-3` folder.






		
[1]: https://leiningen.org/#install
[2]: http://www.luminusweb.net/
[3]: https://code.visualstudio.com/
[4]: https://github.com/BetterThanTomorrow/calva
