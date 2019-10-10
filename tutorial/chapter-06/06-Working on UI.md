# Working on UI

Our back-end API with registration and authentication is ready so it's time to start working on the front-end part. In this chapter we're planning to add a map and integrate it with out API to make it interactive. But first we need to get some general overview of how everything should work together. So we'll start with a theory block again.

Code for the beginning of this chapter can be found in  `app/chapter-06/start` folder.

## Some theory

To implement our UI we've chosen two main libraries: [reagent] and [re-frame]. We'll also need a library to work with maps but we'll get back to it a bit later. As we already know [reagent] is just a wrapper on top of [react] which abstracts DOM manipulations for us. It helps us to build our UI using a bunch of isolated components --- pretty much like LEGO. It has a simple API and really easy to use. Next picture shows an example of an app composed from such components

<p align="center">
  <img src="https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-06/components.svg?sanitize=true">
</p>

But all those components need some data to show. Sure we can fetch that data directly inside a component and for really small applications it works fine. But when an application starts growing and we have a lot of components and some of them need to share data we're in a big trouble: our code will become really hard to maintain. That's why it's a good idea to keep our components and app logic separated. 

And [re-frame] should help us with solving those problems. It gives us a great structure where we can put 


![reframe-img]

[reagent]: https://reagent-project.github.io/
[re-frame]: https://github.com/Day8/re-frame
[react]: https://reactjs.org/
[components-img]: https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-06/components.svg?sanitize=true
[reframe-img]: https://raw.github.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-06/Re-frame.svg?sanitize=true
<!--stackedit_data:
eyJoaXN0b3J5IjpbNTM0Nzk4NzEsMTY4NTAwNDU2NywtMTQ2Nj
A3MzI5N119
-->