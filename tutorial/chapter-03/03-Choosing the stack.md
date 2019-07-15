# Choosing the stack

In this chapter we're gonna decide what tools we're gonna use to build our app. And try to answer why we need those tools. 

## Choosing the tools

It should be clear from the series title that as a main language we'll be using **Clojure**. But besides the language itself we need a database to save our users trips. We need something to help us with project scaffolding because it may take tremendous amount of time and you may even lose all the interest in developing the app itself. And also we need a bunch of libraries to help us with authentication and authorization, routing, view rendering and state managament. 

### Database 

Our app is relatively small and we don't have any special needs so we can probably use **postgreSQL**, **MongoDB** or any other popular solution. But why not using something more clojurish so we'll stick with **Datomic**.
You can read more about it in [their docs  

### 3


[datomic]: https://docs.datomic.com/on-prem/getting-started/brief-overview.html
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTE4NDIxODgxMDksLTE0NDU4NTY0ODAsLT
Q4MTQxOTE0OCwxMjIzNjgwODQ0LC00MzI5OTQxNjIsLTE0NjM3
MDA0NzMsLTEzNDE3ODk3NzRdfQ==
-->