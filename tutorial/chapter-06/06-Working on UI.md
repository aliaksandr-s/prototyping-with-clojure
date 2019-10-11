# Working on UI

Our back-end API with registration and authentication is ready so it's time to start working on the front-end part. In this chapter we're planning to add a map and integrate it with out API to make it interactive. But first we need to get some general overview of how everything should work together. So we'll start with a theory block again.

Code for the beginning of this chapter can be found in  `app/chapter-06/start` folder.

## Some theory

To implement our UI we've chosen two main libraries: [reagent] and [re-frame]. We'll also need a library to work with maps but we'll get back to it a bit later. As we already know [reagent] is just a wrapper on top of [react] which abstracts DOM manipulations for us. It helps us to build our UI using a bunch of isolated components --- pretty much like LEGO. It has a simple API and really easy to use. Next picture shows an example of an app composed from such components

<p align="center">
  <img src="https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-06/components.svg?sanitize=true">
</p>

But all those components need some data to show. Sure we can fetch that data directly inside a component and for really small applications it works fine. But when an application starts growing and we have a lot of components and some of them need to share data we're in a big trouble: our code will become really hard to maintain. That's why it's a good idea to keep our components and app logic separated. 

And [re-frame] should help us with solving those problems. It gives us a great structure where we can put everything related to business logic of our app. It gives us a centralized store where we put all the data related to our app. We can change the data in store using events and effects. And we also can subscribe to those data changes in store, which will also cause our UI to automatically update.

Let's have a look at the next diagram and go through all the steps one by one.

![reframe-img]

We have an application that has a button and an empty table, when user clicks the button we'll get some data from the server and populate the table. 

1. When user clicks the button we dispatch an event which notifies the system that we want to fetch some data.
2. To handle this event we should register an event handler. It will declaratively describe what effects should happen to fetch the data and what to do with the response.
3. We also need to register a few more event handlers that will describe how to update our app db if we get data successfully or we got some errors.
4. By this moment our app db supposed to have all data that we need. So it's time to add a subscription that knows how to get that data.
5. And the last step is to add that subscription to a UI component. In our case it's table. When the data we subscribed to changes, our component UI will be updated automatically. 

That was just a general overview, we'll get into more details when we start working with code.


[reagent]: https://reagent-project.github.io/
[re-frame]: https://github.com/Day8/re-frame
[react]: https://reactjs.org/
[components-img]: https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-06/components.svg?sanitize=true
[reframe-img]: https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-06/Re-frame.svg?sanitize=true
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTI5OTQ3NDc1NCwxNjg1MDA0NTY3LC0xND
Y2MDczMjk3XX0=
-->