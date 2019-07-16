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
eyJoaXN0b3J5IjpbMTUwNDE5NTA4MCwtMTQ3NDYwNzE4MiwxMz
IxNDY2Nzc4LDE0MzY1MDMxOTUsLTEzMDQ0NTY1MDQsLTU2NTc4
NjYwLDU5NzI4MTI4OCwtOTcyNjg5MjkyLC0xNTMwNzQxMDU3LD
E4Njc5MTIzODcsNzEwNTYzNjM3LDIwNzc5NzgwMDksNTg1NzA3
MzU4LDIxMzk0NTQ4NzQsMzIyMzk5NzAyLC0xNDQ1ODU2NDgwLC
00ODE0MTkxNDgsMTIyMzY4MDg0NCwtNDMyOTk0MTYyLC0xNDYz
NzAwNDczXX0=
-->