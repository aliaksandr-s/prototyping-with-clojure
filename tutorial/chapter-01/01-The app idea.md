# The app idea

In this chapter we're gonna describe the app idea and create a few user stories. Also we're gonna draw some simple mockups. 

## The idea description

The main goal of our app is to help travelers to visualize their past and future travels. That actually may sound a bit too official so basically we're building a digital scratch map. Our users will see a world map where they can mark countries they previously visited and countries they would like to visit in future. And of course our users should be able to save their progress. I think that's gonna be enough for the beginning, so let's create some user stories. 

**User Stories:**

 1. As a user I want to see a world map.
 2. I want to be able to track countries I visited.
 3. I want to be able to track countries I'd like to visit in future.
 4. The app should save my progress. 

## Wireframes

We created a few user stories and now we're gonna add some wireframes to fulfill them. I know that this image doesn't look quite like a world map but let's pretend it is. Separate parts represent countries and different patterns show if it's been visited or you're just planning to visit it. 

![map]

With this one image we covered 3 user stories at once, that's basically gonna be the main screen of our app. But we have one story more: the app should be able to save the progress. To implement this we need to add registration and login. For now we're gonna stick with the basic one using email and password but later it can be extended to support authentication with google, facebook or any other authentication provider.

![login]

Here we created a few simple forms for login and registration. Nothing too fancy just a few fields one button and a link. And that's it for this chapter.

[map]: https://raw.githubusercontent.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-01/map.png
[login]: https://raw.githubusercontent.com/aliaksandr-s/prototyping-with-clojure/master/tutorial/chapter-01/login.png
<!--stackedit_data:
eyJoaXN0b3J5IjpbODA1ODU3MzgzLC0xOTk0MzU4OTMwXX0=
-->