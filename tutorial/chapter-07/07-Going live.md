# Going live

Our application is finished and now it's time to make it live so everyone could try it out. In this chapter we will create a production build, then containerize our application for simpler deployment process, and set up a remote server with a custom domain. 

Code for the beginning of this chapter can be found in  `app/chapter-07/start` folder.

## Production Build

All the configs are already set up for us and we just need to provide a `database-url` for production build in `visitera/env/prod/resources/config.edn` file. So it should look something like that 

```clojure
{:prod true
 :port 3000
 :database-url "datomic:free://localhost:4334/visitera_prod"}
``` 

That's all the preparations that we need. Now we can create a production build using `lein uberjar` command. If everything is okay it should create a `visitera.jar` file inside `visitera/target/uberjar` folder. Now we can run and test our production ready application using `java -jar ./target/uberjar/visitera.jar` command. Just not to forget that we should have a running database (`bin/transactor config/samples/free-transactor-template.properties`) before we launch our application.

Our app is running so it's time to test it. Let's go to `localhost:3000` and try to create a new user.
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTE0MDI3MTkzOSwyMDA1NDAyNzEyXX0=
-->