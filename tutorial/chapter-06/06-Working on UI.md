# Working on UI

Our back-end API with registration and authentication is ready so it's time to start working on the front-end part. In this chapter we're planning to add a map and integrate it with out API to make it interactive. But first we need to get some general overview of how everything should work together. So we'll start with a theory block again.

Code for the beginning of this chapter can be found in  `app/chapter-06/start` folder.

## Some theory

To implement our UI we've chosen two main libraries: [reagent] and [re-frame]. We'll also need a library to work with maps but we'll get back to it a bit later. As we already know [reagent] is just a wrapper on top of [react] which abstracts DOM manipulations for us. It helps us to build our UI using a bunch of isolated components --- pretty much like LEGO. It has a simple API and really easy to use.


[reagent]: https://reagent-project.github.io/
[re-frame]: https://github.com/Day8/re-frame
[react]: https://reactjs.org/
<!--stackedit_data:
eyJoaXN0b3J5IjpbMTY4NTAwNDU2NywtMTQ2NjA3MzI5N119
-->