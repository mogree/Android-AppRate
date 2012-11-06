AppRate
=======

* AppRate allows your users to rate your application.

* AppRate shows a customizable rate dialog according to your chosen settings.

How to install and use
----------------------

1. Put the AppRate [jar] in your `libs` folder or add AppRate as a library project.

[jar]: https://github.com/TimotheeJeannin/AppRate/downloads

2. Use AppRate as follows in your `MAIN` activity: 

```java
new AppRate(this).init();
```

Features
--------

* You can decide **not to prompt the user** if the application **has crashed once**.

```java
new AppRate(this)
    .setShowIfAppHasCrashed(false)
    .init();
```

* You can decide **when to prompt the user**.

```java
new AppRate(this)
    .setMinDaysUntilPrompt(7)
    .setMinLaunchesUntilPrompt(20)
    .init();
```

* You can **customize** all the messages and buttons of **the rate dialog**.

```java
AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setCustomTitle(myCustomTitleView)
		.setIcon(R.drawable.my_custom_icon)
		.setMessage("My custom message")
		.setPositiveButton("My custom positive button", null)
		.setNegativeButton("My custom negative button", null)
		.setNeutralButton("My custom neutral button", null);

new AppRate(this)
		.setCustomDialog(builder)
		.init();
```

Screenshots
-----------

![Screenshot 1](https://raw.github.com/TimotheeJeannin/AppRate/master/Screenshots/screenshot_1.png "Screenshot 1")
![Screenshot 2](https://raw.github.com/TimotheeJeannin/AppRate/master/Screenshots/screenshot_2.png "Screenshot 2")