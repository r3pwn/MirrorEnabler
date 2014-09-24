MirrorEnabler
=============

HERE LIES THE MAGIC

Okay, quick rundown of how this app came to be.
* I heard about Google's new Chromecast Screen Mirroring feature and its limited availability.
* I decompiled the new Chromecast apk to see what I could find.
* I ran a ``grep -r mirror | grep string | grep mirror`` (I only run grep mirror the second time for color control)
* That spat out a few strings about mirroring!
* Oddly enough, a week or two before, [@zhuowei](https://twitter.com/zhuowei) taught me about modifying Google Applications by messing with the Google Play Services Database.
* So, just to try, I pulled out my old Kindle Fire HD 7" (2012) and tested it.
* Luckily enough, it worked!
* I then copied and pasted and changed some code from #DebugAllTheThings and voila! You have an app that can enable/disable mirroring on the device that it's running on!
* At first, it would require a reboot, but I found that by force-closing com.google.android.gsf, com.google.android.gms, and com.google.android.apps.chromecast.app, the database's changes were recongized without needing a reboot.
