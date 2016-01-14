Martus(tm) Desktop Software Version 4.5 and Macintosh
---------------------------------------------------------

See https://www.martus.org for information about Martus software.

Requirements to run Martus on a Macintosh
-----------------------------------------

• Officially supported on Mac OS 10.8 and above, and tested on 10.7.3 (Lion) and above.
• Java Runtime Environment (JRE) version 1.8 (sometimes called Java8). The Java community is now supporting JavaFX instead of Swing, and in order to support a multi-lingual Martus, this requires Java8.  You can check which version of Java is running on your machine by opening a terminal window and typing “java -version” (without the quotes) and hitting enter/return.
• 93 MB hard disk space available (100 MB or more recommended)
• 1GB RAM 
• Internet connection, to back up data to the Martus server.  If you do not have an internet connection, see section “9g. Enabling other accounts to send your bulletins to a server.”
• To send bulletins to a Martus Server, your Martus software must not be blocked by a firewall. If you have a software or hardware firewall, it must allow your computer to contact ports 987 or 443, on the Internet. If these ports are blocked when you try to select the server, you will see a message that the server is not responding.
• CD drive (for CD installation only)
• Screen resolution of 960x480 or greater

Note: To display your system specifications in Mac, go to the Apple menu > About this Mac > More info… > System report… > Hardware (this is the Hardware Overview).

Note: If you have multiple versions of Java installed, you will need to launch the Java Preferences dialogue to change the version of Java to be used. To locate it, hit Command-Space to bring up the "Spotlight" search, and type "Java Preferences." Once it is open, you will see two sets of configurations, one for Java applet plugins and another for Java applications. If you wish to change the version of Java Martus uses by default, select the new version using the dialogue instructions, then close out of the Java Preferences screen.  For additional assistance, please email help@martus.org.


To install Martus for Mac OS:
-----------------------------

Note: These instructions may vary depending on which version of Mac OS X you are running. If these instructions are not applicable to what you see on your computer, please email us at help@martus.org.  

Automatic installation using a DMG file (either new or upgrade):

1. Note: Before you install: If an earlier, non-DMG, version of Martus has been installed on this computer you must first delete all of the Martus-related jar files from the /Library/Java/Extensions folder. This is very important, and Martus may not work correctly if you skip this step. You may be prompted to enter your computer administrator username/password to delete these files. The Martus-related jars are: 
       *- InfiniteMonkey.jar
       *bc-jce.jar
       *bcprov-jdk14-135.jar
       *icu4j_3_2_calendar.jar
       *js.jar
       *junit.jar, layouts.jar
       *persiancalendar.jar
       *velocity-1.4-rc1.jar
       *velocity-dep-1.4-rc1.jar
       *xmlrpc-1.2-b1.jar  
After deleting these files, the Extensions folder may be empty. We also recommend that you delete any shortcuts you may have created to the old Martus version, to avoid confusion. 

2. Obtain the Martus DMG file from the Martus website or CD, and double-click it as you would for any DMG file. 

3. If the Martus folder was not automatically opened, double-click on it to view the contents. 

4. While you can copy the Martus application (Martus.app) to your Applications folder, please note that if you do so, it will not be removed when you uninstall Martus, which may be a security concern. 

5. The MartusDocumentation folder contains files with helpful information about Martus, including User Guides, Quick Start Guides, and README files that describe the features in each version, all in various languages. We also suggest that you copy this folder to your computer where you can have easy access to it. 

Note: If you are using a Martus Language Pack (e.g. Martus-en.mlp), please copy the mlp file to your Martus data folder. For instructions on how to access your Martus data folder, see the section below named "Viewing the Martus Data Folder". 

Note: If needed, Armenian, Burmese, Khmer and Bangla/Bengali fonts are provided in the Fonts folder. If you need any of these, you can install it by double-clicking on the .ttf file and choosing "Install". If you have issues running or entering text in Burmese, Khmer, or Bangla/Bengali, please see FAQs 40 - 42 in this Martus User Guide, or email help@martus.org.  


Running Martus on Mac OS X (from a DMG)
---------------------------------------

To run Martus installed from a DMG file, double-click the Martus icon/application (Martus.app file). 

To display additional information as Martus runs that will be helpful in diagnosing any problems in Mac OSX, you can open a Terminal window: 
a) Right-click (cmd + click) the Martus.app file to bring up the popup context menu.
b) From the context menu select "Show Package Contents."
c) The finder will change views to display the "Contents" folder.
d) Navigate to Contents/MacOs.
e) Double click the "JavaAppLauncher"

This will cause a new terminal to start which will contain the console output. 
As long as the terminal is running, and troubleshooting or logging information from Martus will appear there. You can copy and paste the text from the terminal into an email to help@martus.org.


Viewing the Martus Data Folder
-------------------------------

In Mac OS, the Martus data folder is named .Martus and is located in your Home folder. This folder contains your Martus account and bulletin info, as well as other files created while using Martus, such as report/search templates, Contact account files, etc. (though many of these can also be saved to other folders if desired). The Martus data folder is also where you would place any language pack files. Normally the Martus data folder is hidden, so is not viewable using the Finder application. In order to access these files, you will need to set your computer preferences to "show" this hidden folder. To do so, follow these steps:

1. Go to Finder > Applications > Utilities, and open the Terminal application. (You can also hit Command-Space to bring up the search, enter "terminal", and when it finds the Terminal app, launch it.)

2. Type:

    defaults write com.apple.finder AppleShowAllFiles TRUE 

and hit Enter/Return. 

3. Restart the Finder by holding the Option key, and click and hold the Finder icon. When the context menu shows, select Relaunch. (Alternately, you can type the following in the Terminal:

    killall Finder
 
and hit Enter.) 

4. When the Finder restarts, you will be able to view the ".Martus" folder within your home directory and access files saved there. 


Uninstalling Martus on Mac OS X (DMG)
-------------------------------------

If you installed Martus with a DMG file on a Mac, you can uninstall Martus without deleting your Martus bulletins or account data, by simply deleting the Martus application (Martus.app file). 

Note:  Please note that if you copied the Martus application (Martus.app) to your Applications folder or your Desktop (or elsewhere on your computer), it will not be removed when you uninstall Martus, which may be a security concern.

If you want to delete bulletins and your key file, but keep the Martus application, choose Tools > Delete My Data. For more information, see “7. Deleting Your Bulletins and Account Information.”

If you need to remove Martus bulletins and configuration data, as well as the Martus application, choose Tools > Delete All Data and Remove Martus. This feature removes information for all accounts on the computer, not just yours, and should only be used in emergency situations. For more information, see 
“8. Deleting All Martus Data, Including the Application.”

When you remove Martus files using the Delete All Data and Remove Martus command, Martus deletes the files in a way that makes them more difficult to recover than just deleting the files manually. However, to verify that all Martus information has been removed, search for the Martus folder (C:\Martus in Windows) and any folders and files you copied during installation (Mac OS or Linux). If any of these folders or files remain, delete them, and then empty the trash or recycling bin. 

Note: Deleted user data can still be recovered by technicians who gain access to the computer’s hard drive; however Martus deletes it in a way that makes it harder to recover. Additionally, because all bulletin data is encrypted, it is secure as long as your password is strong and remains secret. There are disk utilities available that will completely erase user data in ways that prevent the recovery of any information. 

