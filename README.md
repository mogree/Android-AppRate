AppRate for Android
=============

AppRate for Android is a library that allows your users to rate your application in a non intrusive way. 
A customizable dialog with "rate", "remind later" and "reject" buttons is displayed according to your chosen settings.

How to install/use
------------------

1. Copy the AppRate_0.5.jar into your "libs" folder.

2. Use AppRate as follows in your MAIN activity: 

	new AppRater(this)
		.setMinDaysUntilPrompt(7)
		.setMinLaunchesUntilPrompt(20)
		.init();
