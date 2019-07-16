# Choosing the stack

In this chapter we're gonna decide what tools we're gonna use to build our application. And try to answer why we need those tools. 

## Choosing the tools

It should be clear from the series title that as a main language we'll be using **Clojure**. But besides the language itself we need a database to save our users trips. We need something to help us with project scaffolding because it may take tremendous amount of time and you may even lose all the interest in developing the app itself. And also we need a bunch of libraries to help us with authentication and authorization, routing, view rendering and state managament. 

### Scaffolding

To not spend too much time on a project set up we'll use [**Luminus**][luminus]. It's a mini framework that can give us a fully working environment for development and testing with just one command.

### Database 

Our app is relatively small and simple and we don't have any special needs so we can probably use **postgreSQL**, **MongoDB** or any other popular solution. But why not using something more clojurish so we'll choose  [**Datomic**][datomic]. As a query language it uses [**Datalog**][datalog] which is kinda a mix of logic programming and [**edn**][edn] (a data format, similar to json in javascript world)

### Routing

To distinguish between reqests from the client we need a routing library. [**Reitit**][reitit] is a default one used by [**Luminus**][luminus]. So we'll stick with it.

### Authentication

Authentication and authorization are big and complicated topics by themselvels. And you need a good cryptography background to implement it from scratch. So in our project will use [**Buddy**][buddy] library to abstract all those complex details. 

### View rendering 

Rerendering an application when its state changes used to be a big problem before [**React**][react]. It abstracted everything related to when and how you should change your DOM. When the state of your app changes all the necessary components will be rerendered automagically. To use it in Clojure world we can choose [**Reagent**][reagent] which is just a wrapper on top of [**React**][react]. 

### State managament

Our app should be build out of components and those components should be able to communicate to each other. So will use [**re-frame**][re-frame] to connect all the components into one live system.  

I didn't go into too much details about each tool now. Because we'll get back to each of them in later chapters. But if you're not familiar with some of those tools you might want to have a quick glance at their docs.

[datomic]: https://docs.datomic.com/on-prem/getting-started/brief-overview.html
[datalog]: http://www.learndatalogtoday.org/
[edn]: https://github.com/edn-format/edn
[luminus]: http://www.luminusweb.net/
[reitit]: https://metosin.github.io/reitit/
[buddy]: https://github.com/funcool/buddy
[react]: https://reactjs.org/
[reagent]: https://reagent-project.github.io/
[re-frame]: https://github.com/Day8/re-frame
<!--stackedit_data:
eyJoaXN0b3J5IjpbODI3NDU0Njc4LDE2ODYzNjgzMjIsMTUwND
E5NTA4MCwtMTQ3NDYwNzE4MiwxMzIxNDY2Nzc4LDE0MzY1MDMx
OTUsLTEzMDQ0NTY1MDQsLTU2NTc4NjYwLDU5NzI4MTI4OCwtOT
cyNjg5MjkyLC0xNTMwNzQxMDU3LDE4Njc5MTIzODcsNzEwNTYz
NjM3LDIwNzc5NzgwMDksNTg1NzA3MzU4LDIxMzk0NTQ4NzQsMz
IyMzk5NzAyLC0xNDQ1ODU2NDgwLC00ODE0MTkxNDgsMTIyMzY4
MDg0NF19
-->