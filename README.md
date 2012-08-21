AppRate for Android
=============

AppRate for Android is a library that allows your users to rate your application in a non intrusive way. 
A customizable dialog with "rate", "remind later" and "reject" buttons is displayed according to your chosen settings.

How to install/use
------------------

1. Put in your "libs" folder the AppRate jar. (You can find it on the [download] page.)

[download]: https://github.com/TimotheeJeannin/AppRate/downloads

2. Use AppRate as follows in your MAIN activity: 

	new AppRater(this)
		.setMinDaysUntilPrompt(7)
		.setMinLaunchesUntilPrompt(20)
		.init();
