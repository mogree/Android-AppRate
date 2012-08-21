AppRate
=======

* AppRate allows your users to rate your application in a non intrusive way.

* AppRate shows a customizable rate dialog according to your chosen settings.

How to install/use
------------------

1. Put in your `libs` folder the AppRate [jar].

[jar]: https://github.com/TimotheeJeannin/AppRate/downloads

2. Use AppRate as follows in your `MAIN` activity: 
```java
new AppRater(this)
    .setMinDaysUntilPrompt(7)
    .setMinLaunchesUntilPrompt(20)
    .init();
```