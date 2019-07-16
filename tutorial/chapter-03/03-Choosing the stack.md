# Choosing the stack

In this chapter we're gonna decide what tools we're gonna use to build our app. And try to answer why we need those tools. 

## Choosing the tools

It should be clear from the series title that as a main language we'll be using **Clojure**. But besides the language itself we need a database to save our users trips. We need something to help us with project scaffolding because it may take tremendous amount of time and you may even lose all the interest in developing the app itself. And also we need a bunch of libraries to help us with authentication and authorization, routing, view rendering and state managament. 

### Scaffolding

To not spend too much time on a project set up we'll use [**Luminus**][luminus]. It's a mini framework that can give us a fully working environment for development and testing with just one command.

### Database 

Our app is relatively small and simple and we don't have any special needs so we can probably use **postgreSQL**, **MongoDB** or any other popular solution. But why not using something more clojurish so we'll choose  [**Datomic**][datomic]. As a query language it uses [**Datalog**][datalog] which is kinda a mix of logic programming and [**edn**][edn] (a data format, similar to json in javascript world)

### Routing

To distinguish between reqests from the client we need a routing library. [**Reitit**][reitit] is a default one used by [**Luminus**][luminus]. So we'll stick with it.

### Authentication

Authentication and authorization are big and complicated topics by themselvel 

### View rendering 



### State managament


[datomic]: https://docs.datomic.com/on-prem/getting-started/brief-overview.html
[datalog]: http://www.learndatalogtoday.org/
[edn]: https://github.com/edn-format/edn
[luminus]: http://www.luminusweb.net/
[reitit]: https://metosin.github.io/reitit/
[buddy]: https://github.com/funcool/buddy
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTEyODQ1MjYxMTEsLTEzMDQ0NTY1MDQsLT
U2NTc4NjYwLDU5NzI4MTI4OCwtOTcyNjg5MjkyLC0xNTMwNzQx
MDU3LDE4Njc5MTIzODcsNzEwNTYzNjM3LDIwNzc5NzgwMDksNT
g1NzA3MzU4LDIxMzk0NTQ4NzQsMzIyMzk5NzAyLC0xNDQ1ODU2
NDgwLC00ODE0MTkxNDgsMTIyMzY4MDg0NCwtNDMyOTk0MTYyLC
0xNDYzNzAwNDczLC0xMzQxNzg5Nzc0XX0=
-->