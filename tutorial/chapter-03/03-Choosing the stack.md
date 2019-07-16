# Choosing the stack

In this chapter we're gonna decide what tools we're gonna use to build our app. And try to answer why we need those tools. 

## Choosing the tools

It should be clear from the series title that as a main language we'll be using **Clojure**. But besides the language itself we need a database to save our users trips. We need something to help us with project scaffolding because it may take tremendous amount of time and you may even lose all the interest in developing the app itself. And also we need a bunch of libraries to help us with authentication and authorization, routing, view rendering and state managament. 

### Database 

Our app is relatively small and simple and we don't have any special needs so we can probably use **postgreSQL**, **MongoDB** or any other popular solution. But why not using something more clojurish so we'll choose  [**Datomic**][datomic]. As a query language it uses [**Datalog**][datalog] which is kinda a mix of logic programming and [edn] (a data format, similar to json in javascript world)

### Scaffolding

To not spend too much time for a project set up 
Setting up a project from scratch can take a lot of time and may become a really daunting process. So a reasonable scaffolding may help us a lot because we get a working environment with all the libraries connected with just one command. 

### Routing

### Authentication

### View rendering 

### State managament


[datomic]: https://docs.datomic.com/on-prem/getting-started/brief-overview.html
[datalog]: http://www.learndatalogtoday.org/
[edn]: https://github.com/edn-format/edn
<!--stackedit_data:
eyJoaXN0b3J5IjpbNzEwNTYzNjM3LDIwNzc5NzgwMDksNTg1Nz
A3MzU4LDIxMzk0NTQ4NzQsMzIyMzk5NzAyLC0xNDQ1ODU2NDgw
LC00ODE0MTkxNDgsMTIyMzY4MDg0NCwtNDMyOTk0MTYyLC0xND
YzNzAwNDczLC0xMzQxNzg5Nzc0XX0=
-->