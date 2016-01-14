martus-android
==============

Build Instructions
- Currently only Android 4.0.3, API Level 4 is supported
- Be sure that all your non-Android JARs are exported under Properties->Java Build Path->Order and Export

Eclipse project setup

+ Import Project ODK Collect (./secure-app-vital-voices/opendatakit.collect)
+ Import Project Vital Voices (./secure-app-vital-voices/secure-app-vital-voices)
+ Symlink FormValidator.java into the vital voices src directory
```
	ln -s ./secure-app-vital-voices/opendatakit.formvalidator/src/org/odk ./secure-app-vital-voices/secure-app-vital-voices/src/org 
```