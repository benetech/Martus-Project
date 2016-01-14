Martus(tm) Desktop Software Version 5.1 README.txt
----------------------------------------------------------------

See https://www.martus.org for information about the Martus software.

For all current language packs and translations of the user documentation, 
as well as language packs/documentation for previous versions in various languages, 
go to https://www.martus.org. 

For assistance with Martus, please email martus@benetech.org

System Requirements:
  * Internet connection, if you want to back up data to the Martus servers. To be able to upload to a 
     Martus Server you need unrestricted web access through ports 987 or 443. Note: This is only an issue 
     if operating Martus software from behind a firewall.
  *  Screen resolution of 960x480 or greater

- Windows
  * Windows XP, Windows Vista, Windows 7, or Windows 8 (Window 7 or higher recommended). 

- Mac
  * Officially supported on Mac OS 10.8 and above, and tested on 10.7.5 and above.
  * Java Runtime Environment (JRE) version 1.8.0_40 (sometimes called Java8).

- Linux 
  * This version of Martus does not fully support Linux (there are several display issues, etc.). 
     For a fully supported version of Martus, download version 4.4 of Martus and refer to the 
     version 4.3 Martus User Guide available in the Docs folder of your Martus directory or at 
     https://www.martus.org.  If you want to run Martus 5.1 on Linux, you need Java Runtime 
     Environment (JRE) version 1.8.0_40 (sometimes called Java8) and graphic library gtk2 2.18+ 
     (required for supporting JavaFX).  Some versions of OpenJDK do not include the JavaFX jar, 
	 so you may need to download and install it for Martus to run.


Installing and Running Martus:

- Windows
  1) Insert the Martus CD into your CD-ROM drive or download the Martus installer from 
      https://www.martus.org and run it.
  2) Follow the step-by-step Martus installer instructions.
  3) To start Martus in Windows, choose Start > Programs > Martus > Martus, or double-click the 
      Martus shortcut on the desktop or in the Martus directory.

- Mac
  1) Obtain the Martus DMG file from the Martus website or CD, and double-click it. 
  2) If the Martus folder was not automatically opened, double-click on it to view the contents. 
  3) To run Martus, double-click the Martus icon/application (Martus.app file), or right click and 
      select Open. While you can copy the Martus application (Martus.app) to your Applications folder, 
      please note that if you do so, it will not be removed when you uninstall Martus, which may be a security concern. 

- Linux
  * See note above about Martus compatibility with Linux.


Martus desktop release information by version and release date:

Version 5.1 		2015-04

This release is available in English, Spanish, Arabic, and French, with partial translations in several other languages. 
Other languages will be available in the future, as language-packs on https://www.martus.org 

Changes in this release:  

- Added line graph showing cumulative records over time
- Added option to show a map of where image was taken if geo meta-data is available/included 
- Added display of thumbnails for most image attachments (e.g. jpg, png, bmp, tif)
- Added the record count next to each folder/view label at the top of the main screen
- Simple search on main page can now be activated by hitting Enter after typing in your search terms
- Added ability to use the tab key to progress through record fields when entering data
- Added ability to save attachments to your desktop from inside a record
- Now bring up progress dialog when connecting to the server Sync Manager
- Several look and feel enhancements, including updated/more icons, more screens displayed in JavaFX, and 
  responsiveness of windows for small resolution screens and non-English languages 
- Restrict option to change language to initial login screen
- Upgrade to Java 1.8.0_40
- Added ability to receive/view records from mobile secureApp prototype that are in XForms format

Bug fixes and cleanup, including:
- Fixed occasional issue when switching data entry templates when editing a record that already had data entered
- Imported records folder no longer created with each account, is now only created when importing records  
- Fixed occasional issue on Mac where the user interface would go blank when restoring a Martus account from backup files
- Fixed behavior of Zawgyi font checkbox when displaying Burmese text while running Martus in a language other than Burmese
- Consolidated several options under the Account menu
- Fixed display of certain image attachment file types
- Fixed window title icons for chart screens
- Changed option to move records when in "Search Results" view
- Now show records that are in the Trash in the "All" view
- Wording changes for clarity, consistency, and missing messages
- Added more helpful messages to users for various error scenarios


Known Issues:
- Several display issues in right-to-left languages (e.g. Arabic, Farsi) and in other languages (e.g. Burmese) for some 
   OSs/screen resolutions
- In right-to-left languages (e.g. Arabic, Farsi), during the account setup wizard, the date format sequence field defaults 
   to blank, so you must pick a value explicitly before going to the next step in the wizard or you will receive an error
- Text in the user interface in languages that have not been translated (or where wording has changed from earlier 
   translations) will be marked with brackets
- Very first menu click when opening Martus must be repeated for menu to load
- You are able to open multiple view windows for the same record
- You are not asked to confirm when deleting an item (group of fields) in a multi-item list in a record 
- When you add or delete a record from a list, it will unsort the list.
- Sorting in record lists is case-sensitive (e.g. titles that start with upper-case letters are sorted before any that start 
   with lower-case letters)
- In Windows, some dialogs do not show that Martus is still working as they load (which makes the screen look blank 
   if they take a long time)
- If you have a newer version of a record that is not on the server (but there are earlier versions on the server), 
   downloading records in the Sync Manager or automatic downloads (configured under Server Settings) will cause 
   an error.  You can avoid this by making sure all versions of records are backed up to the server.  If you need assistance, 
   email martus@benetech.org
- If you have not selected the "Have Server Default On?" option under Server Settings, you will need to click the "Server 
   On/Off" button at the top of the main Martus screen twice to turn on your server connection
- In the Advanced Search screen, grid column fields from records sent by the secureApp prototype will have labels that include 
  system tag content.
- If you have deleted a record (so it is in your Trash folder), you will not see it in the Sync Manager.  
- If you have deleted a record with more than one version, and re-download from the server, you will only be able to view data 
  from the final version (Martus will show that there are older versions but you cannot view them).
- Records are referred to as "Bulletins" (pre-version 5.0 wording)in some screens (e.g. Reports, Charts). 
- If your computer date/time is off, you may not be able to access Tor or map display for images.
- It is possible to bring up multiple Sync Manager screens. 
- If you install in Windows in a non-English language, you will still have to select that language on the initial Martus login screen.


Version 5.0.2 		2014-12

This release is available in English, with partial translations in several other languages. 
Other languages will be available in the future, as language-packs on https://www.martus.org 

Changes in this release:  

Bug fixes and cleanup, including:
- Added a graphics setting to avoid Martus crashes on certain versions of Mac OSX with certain 
   hardware/graphics/video drivers.  


Version 5.0.1 		2014-12

This release is available in English, with partial translations in several other languages. 
Other languages will be available in the future, as language-packs on https://www.martus.org 

Changes in this release:  

Bug fixes and cleanup, including:
- You no longer must select language separately for login screen and main UI
- Fixed issue running in right-to-left languages (e.g. Arabic, Farsi) where data entered could be displayed 
  outside of field boxes when editing a record
- Improved behavior when creating a copy of a record that had versions
- Improved behavior when importing template or record files without standard filenames
- Clarified/simplified instructions in multi-part key backup screens


Version 5.0 		2014-11

This release is available in English, with partial translations in several other languages. 
Other languages will be available in the future, as language-packs on https://www.martus.org 

Changes in this release:  

- Improved UI/UX, including new look and feel, more icons, fewer popup windows and password checking, 
   updated terminology and simplified wording
- Auto-sync of retrieving records (your own and your Contacts) from the server, with frequency determined 
   under Server Settings, and improved server side performance 
- Simple text search on main screen. Advanced search from earlier versions available under Analysis menu.
- Tor and Server on/off buttons on main screen 
- Ability to rename folders to Cases/Incidents/custom name as best makes sense for your project 
- Easier management of data entry customized templates, including a Template manager (to load from 
   Contacts or files), and template selector to allow easy switching when creating records.  Added ability to load 
   XML files with custom data entry field definitions into Martus, to allow for easier editing outside of Martus.
- More obvious indication of which fields are required (asterisks) when doing data entry (and visual guidance when 
   try to save without them, i.e. red boxes)  
- Ability to create a new record based off an old one (“Copy”, along with “Move” as ability to put records in 
   different folders) 
- No public section in records 
- Easier to use calendar widgets for dates
- Redoing save paradigm (formerly draft/sealed) – making it more obvious if you are “saving for yourself to edit 
   more later”, vs “creating a new version so you can get back to old version later”, vs “sharing with another Martus 
   user”.  Also separating out an indication of it you want to allow records to be deleted from the servers or not.  
- Improvements to account key backup process
- Removing onscreen keyboard as default 
- Updated most screens to JavaFX
- Bug fixes

Features from earlier releases that are not in 5.0:
- Spellcheck
- Option to download only final versions of records from the server (5.0 will always download all versions)
- The “keep with previous” setting in customizations to put fields on a single line
- Table view for grids (repeating multi-item lists inside records)
- Direct printing of records (can do by creating a page report with all record fields)
- Bulk update to add a new contact to a group of records
- Ability to save image attachments that load in Martus from records to computer (it is possible to save attachments that 
   load with external applications such as Word)
- “Delete all accounts and remove Martus” option


Version 4.5.1 		2014-08

This release is available in English, Burmese, Chinese, Vietnamese, Spanish, Arabic, and other languages as indicated on the https://www.martus.org software download page. Other languages will be available in the future, as language-packs on the documentation page (https://www.martus.org/downloads) 

- Added the Chinese translation of the user interface and Windows Installer.
- Updated Spanish, Vietnamese, and Arabic translations of the user interface.
- Updated Spanish and Vietnamese in-program help and documentation.


Version 4.5		2014-06

System Requirements:
  * Internet connection, to back up data to the Martus server
  * To be able to upload to a Martus Server you need unrestricted web access thru ports 987 or 443. Note: This is only an issue if operating Martus software from behind a firewall.
  *  Screen resolution of 960x480 or greater
- Windows
  * Windows XP, Windows Vista, Windows 7, or Windows 8. 
- Mac
  *Officially supported on Mac OS 10.8 and above, and tested on 10.7.3 (Lion) and above.
  *Java Runtime Environment (JRE) version 1.8 (sometimes called Java8).
- Linux 
  *Version 4.5 of Martus does not fully support Linux (there are several display issues, etc.). For a fully supported version of Martus, download version 4.4 of Martus and refer to the version 4.3 Martus User Guide available in the Docs folder of your Martus directory or here: https://www.martus.org/downloads/4.3/martus_user_guide-v43.pdf.

Installing and Running Martus:

- Windows
  1) Insert the Martus CD into your CD-ROM drive or download the Martus installer from https://www.martus.org and run it.
  2) Follow the step-by-step Martus installer instructions.
  3) To start Martus in Windows, choose Start > Programs > Martus > Martus, or double-click the Martus shortcut on the desktop or in the Martus directory, or choose Start > Run and then type the following text: 
   C:\Martus\bin\javaw.exe -jar C:\Martus\martus.jar
- Mac
  1) If an earlier, non-DMG, version of Martus has been installed on this computer you must first delete all of the Martus-related jar files from the /Library/Java/Extensions folder. After deleting these files, the Extensions folder may be empty. We also recommend that you delete any shortcuts you may have created to the old Martus version, to avoid confusion. 
  2) Obtain the Martus DMG file from the Martus website or CD, and double-click it. 
  3) If the Martus folder was not automatically opened, double-click on it to view the contents. 
  4) To run Martus installed from a DMG file, double-click the Martus icon/application (Martus.app file).While you can copy the Martus application (Martus.app) to your Applications folder, please note that if you do so, it will not be removed when you uninstall Martus, which may be a security concern. 
- Linux
  * See note above about Martus compatibility with Linux.

Changes in this release:  
- Added Martus Configuration Wizard for account setup, using JavaFX for newer look and feel.
  1) Simplified server setup with integrated Martus default server information. (also included in 
      Select Server screen outside of wizard)
  2) Add and verify Contacts (formerly Headquarters and Field Desk accounts) during setup. To 
      improve usability, contact key information can be saved to and loaded from Martus servers 
      (in addition to via a file if access to a server is not available). Increased account public codes 
      to 40 digits for security, but allow identification of accounts via a short access token with visual 
      verification of the longer public codes for usability. (also included in Manage Contacts screen 
      outside of wizard)
  3) Import forms (Martus generic bulletin templates and custom templates from Contacts) during 
      setup.  Also added ability to import templates from any Martus user (not only Contacts), and to 
      save templates to and load templates from Martus servers (in addition to via a file if access to a 
      server is not available).
  4) Other items in Configuration wizard include:  single encrypted file key backup and setting of 
      preferences (including Tor).
- Added the Vietnamese translation of version 4.4 user interface and Windows Installer.
- Updated Farsi/Dari, Russian and Spanish translation of user interface to the 4.4 version, as well as 
  updating the Russian User Guide and in-program help.
- Fixed bug so that you cannot add yourself as a contact (in earlier versions, an account could add 
   itself as its own Headquarters or Field Desk, which could cause confusion)
- Upgraded to Java8, which is included in the Martus Windows Installer
- Account contact information is no longer sent to a Martus server. 


Version 4.4 		2013-11

This release is available in English, Burmese, and other languages as indicated on 
the https://www.martus.org software download page. Other languages will be 
available in the future, as language-packs on the documentation page 
(https://www.martus.org/downloads)

- If sending a bulletin to the server is interrupted due to connectivity issues, Martus will now resume 
  from the point where it left off, instead of starting over, which will make sending faster, especially 
  when the bulletin has large attachments.
- Added the Burmese translation of the Windows Installer, and updated Burmese translation of user 
  interface and documentation to the 4.3 version.
- Corrected display of several items in Burmese, including in Date Ranges, Search, Customization, and 
  Retrieve dialogs.  
- Adjusted the size of the Windows "Incremental" Installer download files on the website so that there 
  aren't so many files to download, but they are still significantly smaller than the full download (if you 
  have a slow or unreliable internet connection, you may prefer to download Martus in smaller pieces, 
  so that a download interruption will only affect the piece being currently downloaded).
- Several other minor bug fixes, clarifications and clean-ups to the user screens.   


Version 4.3 		2013-08

This release is available in English and other languages as indicated on 
the https://www.martus.org software download page. Other languages will be 
available in the future, as language-packs on the documentation page 
(https://www.martus.org/downloads)

Note for Windows users:  
  You cannot upgrade from earlier versions of Martus to the 4.3 version.  You need to uninstall your 
  earlier version of Martus (either under Start > Programs > Martus > Uninstall Martus, or using 
  Add/Remove Programs in the Control Panel).  If you are running Windows Vista or Windows 7 and 
  had a Martus shortcut in your Start menu, you may also need to remove the Martus group by going 
  to Start > Programs, right-clicking on the Martus group, and picking "Delete".  Otherwise those 
  links will remain when you uninstall Martus 4.3, which could be a security issue.

- Security: 
  1) Integrated Tor into Martus, via the Orchid software, developed by Subgraph.  You can 
  turn on Tor in Martus by going to Options > Preferences. Tor can improve security, but may 
  making sending bulletins slower.  You can learn more about Tor at  https://www.torproject.org/. 
  2) The Windows installer now includes Java 7, and Martus will run with Java 7 on other operating 
  systems (including OpenJDK 7 on Linux).
  3) Added ability to view full Public Account ID of Headquarters or Field Desk accounts you have configured, so 
  that you can check with the owners of those accounts to make sure you have the correct information.  
  4) Updated Uninstaller to delete the Martus group in the Start menu in Windows 7 and Vista.
- Usability: 
  1) Fixed several issues in File/Open and File/Save functions, including where filenames would be 
  removed or have extensions added to them when navigating to different folders.  
  2) Set the Martus  account directory as the default folder where files will be saved (though you can 
  choose to save elsewhere if desired).  
  3) Allow you to select where you would like to save your exported public information file (.mpi) for 
  secure sharing with other Martus accounts, instead of always saving the the Martus account directory.    
  4) Added explanation to error screen when backing up your key.  
  5) Added filters to each place you load files so that only the correct type is shown in the file 
  navigator (e.g. .mpi files).  
  6) Changed color of verified field desk labels in bulletins to green, to distinguish from yellow warning 
  label for unverified field desks.  
  7) Removed potentially confusing "list of fields" dialog when initially creating an account.  
  8) Added a scroll bar to the customization error code screen so that it is more easy to view when on 
  a small screen.
Translations: 
  1) Corrected display of several items in Burmese, including "Load Values", section headers, and page reports.  
  2) Added additional instructions for translators to Martus Translation File (.mtf).
  3) Fixed issues with font display on final page of Windows installer for Russian, Arabic, Farsi, and Thai.
  4) You can now install Martus in any supported language regardless of what language your Windows OS is set to 
  and it will display correctly.
  5) Added the Khmer translation of the Windows Installer.
- Updated to new Martus logo.
- Several other minor bug fixes, clarifications and clean-ups to the user screens.   


Version 4.2 		2013-04

This release is available in English and other languages as indicated on 
the https://www.martus.org software download page. Other languages will be 
available in the future, as language-packs on the documentation page 
(https://www.martus.org/downloads)

- Performance improvements:  Dramatically increased speed for loading the retrieve screen.
- Burmese font updates:  allow data entry and display in commonly-used Zawgyi font 
  automatically when running Martus in Burmese.  If you are running Martus in a language 
  other than Burmese and want to type or view Burmese data in your bulletin fields, choose 
  Options > Preferences, and check the "Use Zawgyi font for Burmese display and input" box.  
  Added Zawgyi font to Martus install.
- Security enhancements: 1) added additional clarification in software and documentation about 
  how account contact information is used/accessed on the servers; 2) increased the RSA key size 
  for new accounts; 3) improved the random number generation used in creating multi-file 
  key "share" backups (if you had already created a multi-file key backup, you will be prompted 
  to recreate the pieces using the new method); 4) added Field Desk approval mechanism so that 
  you are warned if you (as a Headquarters account) are sent bulletins from a field desk account 
  you have not verified.  You are also warned if you try to open attachments or modify bulletins 
  from an unverified Field Desk account; 5) added instructions to User Guide on how to run 
  Martus more securely with Tor (to make sending your data to the servers harder to recognize) 
  and TAILS (bootable/live USB).
- Updated translations:  Spanish and Burmese software translations were updated to include 
  the new features in 4.1 (Charts, Spellcheck)
- Upgraded to latest versions of BouncyCastle and XMLRPC
- Fixed issues when trying to save reports and other files when running Martus directly 
  from a Mac dmg file
- Removed MESSAGE fields from list of fields for creating Charts
- Added Armenian font to upgrade Windows Martus install
- Java updates:  Upgraded Windows install to include latest version of Java6.  Updated 
  Linux install to be able to run on OpenJDK (6 or 7), which also applies to running 
  Martus with Tails (see User Guide FAQs).
- Several other minor bug fixes, clarifications and clean-ups to the user screens.   


Version 4.1.1 		2012-08

This release is available in English and other languages as indicated on 
the https://www.martus.org software download page. Other languages will be 
available in the future, as language-packs on the documentation page 
(https://www.martus.org/downloads)

- Fixed an issue when running Martus 4.1 in languages other than English, that 
  cut off initial wording in Charts and Spell Check Dictionary screens.
- Added 4.0 Burmese and Armenian translation updates. 


Version 4.1 		2012-07

This release is available in English and other languages as indicated on 
the https://www.martus.org software download page. Other languages will be 
available in the future, as language-packs on the documentation page 
(https://www.martus.org/downloads)

- Added initial spell-check functionality for text fields in bulletins in English. When 
  editing a bulletin, mis-spellings will be noted by red underlining, and you can 
  see suggestions or add the word to the user dictionary by right-clicking on the 
  word.  Please note that the spell-checker will ignore words that are all capital 
  letters or contain numbers, the spell-checker in this version does not check any 
  fields outside of bulletins (e.g. contact info, HQ labels, folder names), and the 
  spell-checker only displays mis-spellings in bulletin fields when in Edit mode 
  (they are not displayed in View mode). For words that are mis-spelled inside of 
  Grid text fields, you have to left-click on the cell first (as if you were editing it) 
  before right-clicking to bring up the menu to see suggestions or add to the user 
  dictionary.  You can also view the spell-check dictionary and add to or delete 
  words from it if desired under the Options > Spell Checking menu.
- Added initial chart functionality - options are bar chart, 3d bar chart, or pie chart 
  (showing %s) on any single Martus bulletin field that appears in any of your 
  bulletins.  Please note that charts count the number of Martus bulletins that 
  match the search criteria defined when you create the chart, Martus only displays 
  data from the latest version of each bulletin on your computer in the report, and 
  you will see a warning message that any data in charts will not be encrypted (so 
  anyone who sees the chart in printed or electronic form will be able to read all 
  the data).
- Initial Martus video tutorials added to the Martus website: Using Martus, Installing 
  Martus (Windows and Mac), Troubleshooting in Martus (Windows and Mac)
- Added alpha test implementation of XML export of multiple versions of Martus bulletins. 
  This feature has not been fully completed, but will be helpful for those users who 
  want to export all their version history into other tools.  Please note that if you try to 
  import a multiple-version XML file exported from Martus, the imported bulletins will not 
  be connected to each other in any way, and would not have any previous author 
  information. To access this feature, you must add the “ --alpha-tester” option to your 
  Martus command line. For more information, see section “11. Frequently Asked Questions” 
  (FAQ) number 50 (Is it possible to export more than the final version of my bulletins to 
  XML?) in the version 4.1 Martus User Guide. 
- Added Armenian to bulletin language list, and an Armenian unicode font to be used by 
  Martus to the installers
- Added right-click Cut/Copy/Paste/Delete/Select All menus for custom field grid cells.
- Several other minor bug fixes, clarifications and clean-ups to the user screens.   


Version 4.0 		2012-05

This release is available in English, Farsi/Dari, Spanish, Burmese and other languages as indicated 
on the https://www.martus.org software download page. Other languages will be available in the future, 
as language-packs on the documentation page (https://www.martus.org/downloads)

- Update to Java6 (sometimes called 1.6).  Java6 may improve performance 
  in some instances, and improves font display for several languages 
  (E.g. Khmer, Farsi/Dari) and requires less manual effort by users for correct 
  font display (E.g. Burmese, Khmer, Bengali/Bangla). The Windows installers 
  automatically include Java6; on Mac/Linux you should make sure that you are 
  running Java 1.6.0_30 or later, as earlier versions of Java6 could cause Martus 
  to not work correctly.  You can check which version of Java is running on your 
  machine for Mac/Linux by opening a terminal window and typing “java –version” 
  (without the quotes) and hitting enter/return.
- Added initial “memory switches” to Windows and Mac shortcuts to help Martus run 
  more smoothly for accounts with large numbers of bulletins or large bulletins.  User 
  can still increase memory values if needed, but this should resolve issues for many 
  cases that previously needed to be manually created.
- Enhanced Martus user interface display in Windows, to use the default system 
  “look and feel”. 
- Due to security issues discovered in early versions of Windows and Java, Martus 4.0 
  will only run on versions of that are newer than Windows 98 (ME, 2K, XP, 2003, 
  Vista, 7). If you have Windows98/NT or older, you need to use Martus 3.6.2 or earlier, 
  but we highly recommend that you try to upgrade to a more secure Windows version 
  if possible.
- Added additional information to the Martus console log for easier debugging / 
  troubleshooting of problems reported by users. Information added includes the Martus 
  public code for an account and current memory information.
- Developed a new process for building (creating) Martus releases (for Windows, Mac, 
  and Linux), that makes testing and deployment much faster and contains several other 
  internal enhancements. 


Version 3.6.2             2011-10

This release is available in English, Farsi/Dari, Spanish and other 
languages as indicated on the https://www.martus.org software download 
page.  Other languages will be available in the future, as language-packs 
on the documentation page (https://www.martus.org/downloads)

- Fixed issue when searching for entries in dropdown fields that have spaces 
  at the beginning or end of the entry.
- Fixed issue when loading a previously saved search that contains fields 
  that no longer exist in any bulletins on your computer.  Instead of receiving 
  an error that does not allow the user to search, the row in the search grid 
  containing those fields will be reset to the default/new search row values 
  (i.e. "Any Field contains")


Version 3.6.1             2011-10

This release is available in English, Farsi/Dari, Spanish and other 
languages as indicated on the https://www.martus.org software download 
page.  Other languages will be available in the future, as language-packs 
on the documentation page (https://www.martus.org/downloads)

- Minor updates to English documentation and search help screen
- Minor updates to Farsi/Dari user interface translation and addition 
  of Farsi/Dari README 
- Updates to Spanish user interface translation and README


Version 3.6.0             2011-09

This release is available in English, Farsi/Dari and other languages as 
indicated on the https://www.martus.org software download page.  
Other languages will be available in the future, as language-packs 
on the documentation page (https://www.martus.org/downloads)

- Added data-driven dropdown fields (dropdown lists created by data entered 
  in a bulletin grid) to list of available fields to search on in Search screen.
- Added scanning for and loading values entered in bulletin fields in “Search for…”
  box for text and data-driven dropdown field types in Search screen.  When 
  picking text fields to search on in the field selection screen, users can select the 
  “Load all possible values for selected field” checkbox to populate the “Search for…” 
  box with the values that were entered in all the bulletins on the local computer if 
  desired. For data-driven-dropdowns, the values that were entered in bulletins are 
  automatically loaded when the field is chosen in the Search screen. There is also 
  a “Load all possible values for selected field” button in the main Search screen, if 
  users want to load field values without going into the field selection screen (for 
  example, after loading a previously saved search, or if they did not select the 
  checkbox in the field selection screen).  Loaded values are sorted alphabetically, 
  not in the order they were entered in the data source grid. 
- Changed data-driven dropdown values when editing bulletins to be sorted 
  alphabetically instead of in the order they were entered in the data source grid, 
  for easier data entry.
- Headquarters accounts now can retrieve updated field draft bulletins without deleting 
  prior versions.  
- Fixed an error where Martus did not remember your previous Search criteria if the 
  search criteria contained the “Last Saved Date” field 
- Clarified error messages to user when Headquarters account tries to seal bulletins 
  created by a field desk account.  A headquarters account cannot itself directly seal 
  (either individually or in bulk) retrieved field draft bulletins, but can modify retrieved 
  field draft bulletins, becoming the owner of those drafts, and then can seal them.  
- Fixed an error that omitted data from reusable dropdown columns inside a grid when 
  printing and sending any public portions of bulletins to the Martus Search Engine.  
- Updated Dari user interface translation and added Afghan calendar preferences options 
- Fixed display of reusable dropdown fields to be right-justified for right-to-left languages.  
- Several other minor bug fixes, clarifications and clean-ups to the user screens.


Version 3.5.1             2010-11   

- Fixed an issue with reusable choices dropdown lists where values 
  with overlapping labels were not distinguished during data entry.
- The search screen now sorts dropdown entries alphabetically, so 
  that if multiple customizations had the same field with different 
  dropdown values, the combined lists will be sorted, instead of new 
  values appearing at the bottom of the previous list.
- Fixed an issue where modifying a bulletin was not detecting if a 
  reusable choice dropdown list had been altered. Martus now correctly 
  asks if you want to use the old or new values. 
- Fixed an issue that would reset entries to previous value when 
  appending rows to grids with dropdowns in expanded view.


Version 3.5.0             2010-10

- Added a new customization feature that allows multi-level 
  dropdown lists, so that the user can define lower level values 
  that are dependent on the higher level value (e.g. for locations, 
  you can define state and city values where only cities that belong 
  to a particular state are displayed once the state value is selected).  
  The lists of choices you create for these dropdown fields are 
  "reusable" so they do not have to be typed more than once in your 
  customization definition.
- Allowed setting of default values for dropdown and text fields for 
  more efficient data entry.
- Fixed issue where data entered in a grid cell would not be saved if 
  you did not tab out of the cell or hit enter to create a new row in 
  the grid.
- Fixed issue where certain fields could show up more than once in 
  search and report field selection lists.  
- Added instructions for Bengali/Bangla, Burmese, and Khmer font 
  display in Martus to the User Guide.


Version 3.4.1             2010-08

- Changed the Martus time-out length when there is no activity to 
  be more flexible, instead of always timing out at 10 minutes. This 
  can help improve security if you are working in a public place or 
  on a shared computer, and can also improve the performance of 
  long operations (such as retrieval of bulletins from the server).  
  Different time-out lengths are allowed by setting a parameter in 
  the command line used to run Martus.  The parameter is 
  "--timeout-minutes=X" where X can be any number of minutes.  
  For example, to set the timeout length for Martus running on your 
  computer in Windows to always be 5 minutes for a higher level of 
  security, change the command line in the desktop shortcut (right-click, 
  and choose Properties) to the following: 
C:\Martus\bin\java.exe -Xbootclasspath/p:C:\Martus\lib\ext\bc-jce.jar -jar C:\Martus\martus.jar --timeout-minutes=5
  For instructions on how to change the Martus command line for Mac or Linux, 
  see section 2a of the User Guide or email info@martus.org.
- Fixed an issue with very large customizations that would cause Martus to 
  load very slowly and result in various errors.


Version 3.4.0             2010-03

- Added the ability to require data entry in certain grid 
  columns before a bulletin can be saved. 
- Added the ability to specify that all grid column specifications 
  in the Search screen be matched to a single row of bulletin grid 
  data. To do this, check the 'Match grid column specifications' 
  checkbox in the Search screen.  For example, if you want to search 
  for a specific victim name in a single row in your bulletins created 
  after a certain date, select the checkbox and enter the following 
  fields in the Search screen: "Victim Information: First Name" = x 
  and "Victim Information: Last Name" = y and "Date Created" >= YYYY-Mon-DD. 
  If you do not select the "Match grid column specifications" checkbox, 
  Martus will find bulletins created after your specified date where any 
  row has the first name you specified and any other row has the last 
  name specified, but not necessarily in the same bulletin row (you could 
  have a row with "First Name" = x and "Last Name" = b, and a different 
  row with "First Name" = a and "Last Name" = y, and Martus will find that 
  bulletin as matching the search because you did not specify that it had 
  to match in a single row).
- Added the ability to require that Martus validate all dates against 
  user-specified date ranges in the bulletin customization  (E.g. event 
  dates cannot be before or after a certain date, including the ability 
  to say that dates cannot be after “today”, i.e. in the future). 
  Please note that you can require date validation of standard 
  fields as well as custom fields. Date field Year dropdown choices 
  will reflect the hard-coded date ranges specified in the customization.
- Added ability to capture 'Extended bulletin history' across users. This 
  is viewable under the Bulletin Details button, so that a Headquarters 
  account who creates a new version of the bulletin can see the public 
  code and bulletin id of the previous author’s versions of the bulletins. 
  The previous author and bulletin identification information is also added 
  to the XML export of any bulletins with this extended history.
- Fixed Modify existing bulletin functionality so that Martus recognizes 
  whether the always Private section customization has changed, and asks 
  the user if they want to use the old or the new fields. 
- Added a warning message if a user tries to import an incompatible xml 
  file created by a different version of Martus.
- Several minor bug fixes, clarifications and clean-ups to the user screens.
- Updated version of English User Guide 


Version 3.3.2             2009-08

- Fixed issues related to expanded grids that contain data-driven-
  dropdowns, such as user interface slowness or freezing when tabbing 
  out of a field in an expanded grid that is a data-driven-dropdown
  source (and the data-driven-dropdown is also in an expanded grid), 
  collapsing a grid that contains a data-driven-dropdown, or saving 
  a bulletin with an expanded grid that contains a data-driven-dropdown. 
- Fixed incorrect functionality of custom multiline fields not 
  being displayed when creating page reports. Please note that
  page reports created with earlier versions of Martus will still 
  have the problem - users will have to create new page reports to 
  see the fix.


Version 3.3.0             2008-08

- Added the ability to 'batch' seal multiple drafts at once. To 
  do this, go to Edit > Seal bulletin(s).
- New feature to add a Headquarters to a group of bulletins or 
  to a an entire folder of bulletins at once. To do this, go to 
  Edit > Update headquarters access. This option will be grayed 
  out unless at least one bulletin is selected and at least one 
  HQ is configured. Martus will display a progress bar during the 
  update and allow you to cancel if desired. Draft bulletins will 
  be updated to reflect the new HQ information. For sealed 
  bulletins, Martus will automatically generate a new sealed 
  version of each bulletin.
- Added the ability to view several types of image attachments 
  inside Martus while previewing, creating, and editing bulletins, 
  as well as in the Bulletin Details preview.
- Several minor bug fixes, clarifications and clean-ups to 
  the user screens.


Version 3.2.0             2007-09

- New bulletin display functionality allows you to:  1) hide/unhide 
  fields in bulletins for long fields that take up a lot of space, 
  2) create sections in your bulletins which you can also 
  hide/unhide, 3) display grids in an "expanded" view (including 
  the ability to add rows from that view), 4) put fields next to 
  each other on a row, 5) reduce the default size of grids to save 
  space.
- Added the ability to require certain fields to be entered before 
  a bulletin can be saved. 
- Introduced the ability to create "data-driven" dropdowns, where 
  the values in a dropdown field are based on data that has been 
  entered in a grid field elsewhere in the bulletin.  Please note
  that the data source and resulting dropdown cannot be in the 
  same grid field.
- Added a Search progress status dialog, and the ability to cancel 
  Searches.
- Improved performance for Searches and other bulletin operations, 
  especially for users with large numbers of bulletins. 
- Fixed several issues, including:  1) configuration settings (e.g. 
  HQs) would not be saved for users with very large bulletin 
  customizations, 2) errors when dragging bulletins while sending 
  to/retrieving from a server.
- Improved messages to users (e.g. customization errors)
- Several minor bug fixes, clarifications and clean-ups to 
  the user screens.


Version 3.1.0             2007-04

- New functionality notifies HQs if there are field bulletins 
  to retrieve. To enable this in your Martus account, go to 
  Options > Preferences, and select "Automatically check for new 
  Field Desk bulletins." Approximately hourly, a message will 
  appear in the status bar (lower left corner of the screen) 
  saying that Martus is checking for new field desk bulletins. 
  The status bar will display another message if there are field 
  bulletins to be retrieved, at which point you can go to the 
  Server menu to load the Retrieve screen.  
- Added the ability to sort bulletins in the Retrieve screen 
  by clicking on the column headers. The sort functionality is 
  ascending only.
- Improved performance and possible memory issues during login 
  and exiting Martus for users with large numbers of bulletins. 
- Added a mechanism to speed up Martus loading and navigation 
  when an account has a large number of bulletins.  You can add 
  " --folders-unsorted" to the end of the command in your Martus 
  Desktop shortcut.  This will cause folders in Martus to be 
  unsorted when you initially load them (since sorting can take 
  time with a lot of bulletins.)  You can always click on a column 
  header in the bulletin preview list to sort the folder if desired, 
  but including this option will save time on startup and on 
  entering new folders in Martus.
- Fixed the following bugs introduced in version 3.0, including: 
  1) Date range fields not showing up on page reports, 
  2) Private section customization being lost when exiting 
  Martus, 3) Issues selecting files on a Mac (e.g. while 
  configuring HQs, attaching files to bulletins, restoring key, 
  and importing customization template,) 4) Inconsistent Arabic 
  date display in reports between bulletin details and summary 
  counts. 
- Several minor bug fixes, clarifications and clean-ups to 
  the user screens.


Version 3.0.0             2006-09

- Added report functionality.  Reports display results for 
  bulletins that match a certain search criteria, and can 
  be printed or saved to a file.  Reports can contain subsets 
  of bulletin fields, and can be formatted as a table with 
  one row for each bulletin.  Reports can be grouped and 
  sorted by several fields, with a summary count of bulletins 
  in each grouping.
- Added the ability to customize the format of the bottom/Private 
  section of bulletins.
- New Import functionality allows users to import electronic 
  data into Martus bulletin format, including both text and 
  attachment files. Export functionality was also updated to 
  match the Import structure, and now allows exporting of 
  attachments.
- Enhancements to Search functionality include the ability to
  search on particular columns within a customized grid, merging 
  similar fields in the field-selection-list, and clarifications 
  if multiple fields have the same labels.
- Changed default year in bulletin dates to be "Unknown", 
  instead of the current year
- Significant performance enhancements for accounts with 
  large numbers of bulletins, and specifically to loading of the
  Retrieve screen. Additional status messages to users during 
  potentially long operations.
- Customization enhancements, including additional messages 
  to users, and displaying long custom field labels on 
  multiple lines


Version 2.9.0             2006-03

- As of 2.6 Server release (March 2006), sped up several 
  major client/server operations.  Specifically, the following 
  operations are now faster: uploading/sending bulletins, 
  retrieving your own bulletins or field office bulletins, 
  initialization when connecting to the server at account login.
- Performance improvements for accounts with a large number of 
  bulletins.  Impacts the speed of the following actions:  
  display of folders/sorting/moving bulletins etc
- Changed Retrieve operations so that they happen in the 
  background (similar to sending bulletins to a server), so that 
  you can continue to work in Martus while that is happening 
  without waiting for the retrieval to finish.  When it is 
  complete, bulletins are displayed in the appropriate 
  "Retrieved" folder.  To cancel a Retrieve, go back into the 
  Retrieve dialog.
- Improved messages to the user about server status.
- Added the ability to search on columns within a grid (instead 
  of the entire text of the grid) when specifying field in an 
  Advanced Search, and added the option to search only on the 
  most recent version of bulletins.
- Fixed search bugs introduced in 2.8 and 2.8.1.  Specifically, 
  addressed incorrect search results on dropdown fields with 
  spaces in the choice values, and incorporated the 
  customization tags in the search field list where labels were 
  left blank (e.g. for section headers) so that there are no 
  empty values in the field list. 
- Added ability to insert and delete rows in customized grids 
  and search screen
- Use all available screen space when displaying bulletin data 
  and the Contact info dialog.
- Moved "Resend Bulletins" to be under the Server menu (for HQs 
  that back up bulletins to servers for field offices that do not 
  have access to the internet)
- Removed misleading "not all bulletins were retrieved" 
  messages that appeared when a HQ account did not have 
  permission to view old versions of certain bulletins
- Several updates to date preferences:  localization of date 
  formats, additional format choices, changes to correctly 
  display (and convert previously entered) Thai and Persian 
  dates.  Persian dates use a well-known arithmetic algorithm 
  for calculating leap years.  Also created tool to help diagnose 
  date settings.  
- Made change to help word processing programs correctly 
  display accents in html report files.
- Added Kurdish to the language dropdown list.  If you need 
  help with the display of Kurdish fonts in Martus, please 
  contact help@martus.org .
- Initial implementation of data import tool to allow 
  conversion of electronic text files (in .csv or .xml format) 
  to Martus bulletin format.  This initial version does not 
  handle import of customized grids or attachments, but will 
  handle all other field types.  For instructions/help on 
  running this utility, please contact help@martus.org .
- Numerous minor bug fixes, clarifications and clean-ups to 
  the user screens.


Version 2.8.1             2005-11

- Fixed a problem introduced in version 2.8 where dates and 
  date ranges with values earlier than January 1st 1970 were 
  displayed, and could be stored, incorrectly.
- Incorporated Thai and Russian 2.8 software user interface 
  translations


Version 2.8.0             2005-09

- Added ability to create grid columns of different types 
  (drop-down lists, checkboxes, dates, and date ranges)
- Advanced search capability now allows users to specify 
  particular fields in which to search (including custom fields), 
  in addition to searching the entire text of bulletins.  Searches 
  can combine searches in different fields using and/or options.
- Enhanced printing functionality to be able to print multiple 
  selected bulletins at once
- Added ability to save selected bulletin(s) to a html text file, 
  with option to include or exclude private data
- Created a new "Organize Folders" menu option to allow users to 
  put folders in any order
- Added warnings to users if the translation they are running is 
  not the same version as the software, and display the date of 
  any language packs in the About box
- Bring up Martus logo as soon as program is started so users know 
  that it is loading 
- Updated encryption libraries to use Bouncy Castle Java 
  Cryptography Extension
- Additional improvements to display and printing of right-to-left 
  languages
- Incorporated system fonts for menu display of certain languages 
  (e.g. Nepali)
- Changed bulletin behavior so that modifications to drafts pick 
  up newly configured Headquarter and customization settings
- As of the 2.4 Martus Server release, added ability for Martus 
  Client users to receive news from a Martus backup server when 
  they connect (e.g. messages about new versions available for 
  download, or server maintenance downtime)
- Modified method of verification for downloaded files from MD5 
  to SHA1
- Updated English user documentation (Quick Start and User Guides) 
- Numerous minor bug fixes, clarifications and clean-ups to the 
  user screens.


Version 2.7.2             2005-08

- Removed incomplete/inaccurate Nepali 2.0.1 software user 
  interface translation, and placed an updated Nepali language 
  pack on https://www.martus.org/downloads.   This language pack 
  includes Nepali translations of the software user interface 
  (usable for versions 2.5 and later, with 90% of the strings 
  translated into Nepali), in-program help (version 2.0.1), 
  Quick Start Guide (version 2.0.1), User Guide (version 2.0.1), 
  and README file (partially translated up to version 2.6).  

In Windows, to run Martus in Nepali for version 2.7.2 and earlier 
so that all the menus will show up correctly, you must make a minor 
modification to the command used to start Martus (from the command 
prompt, and in any desktop/Start Menu shortcuts or aliases that were 
created when you installed).  

To run from the command line, go to your Martus directory and type:
C:\Martus\bin\javaw.exe -Dswing.useSystemFontSettings=false -jar C:\Martus\Martus.jar 

To change your shortcuts, right-click on them, choose Properties, 
and change the Target command to:
C:\Martus\bin\javaw.exe -Dswing.useSystemFontSettings=false -jar C:\Martus\Martus.jar 


Version 2.7.0             2005-04

This release is only available in English and Persian.  
Other languages will be available in the future, as language-packs 
on the documentation page (https://www.martus.org/downloads)

- Added ability to create single custom field "drop-down" 
  lists (not within a grid)
- Added ability to create custom field "messages" to give 
  guidance on how to enter data, and to create comments/notes 
  that will be displayed in every bulletin (e.g. on-screen help)
- Added ability for a Headquarters account to export customization 
  templates to give to field users, or users to export their own 
  templates.  Users can then import customization settings from a 
  choice of templates.
- Each configured Headquarters account can now be enabled or disabled 
  for each bulletin that is created or modified.  Users can also 
  designate certain Headquarters accounts to be assigned to all 
  newly created bulletins by default.
- Searches now scan previous versions of each bulletin in addition 
  to the latest version 
- Additional improvements to display of right-to-left languages
- Persian translation of user interface included
- Export to XML now includes custom field type 


Version 2.6.0             2005-02

- Users can now search and view the full contents of all 
  versions of sealed bulletins stored on their computer, by 
  clicking on the Bulletin Details button.  
- Added ability to choose whether to retrieve all versions or 
  only the most recent version of a sealed bulletin from the 
  server.  Users with small disk drives or slow internet 
  connections may choose to only retrieve the most recent 
  version for large bulletins. 
- Attachment filenames are now included in searches. 
- Changed XML export functionality to further support custom 
  fields and sealed bulletin versions.
- Enhancements to display of Right-to-Left languages (e.g. Arabic)
- Inclusion of Arabic User Guide and Quick Start Guide.
- Several minor bug fixes, clarifications and clean-ups to the 
  user screens.
- There may be issues with the display of some screens when running 
  Martus 2.6 in Arabic in a Mac OS.  


Version 2.5.0             2004-11

- Added ability to create new versions of sealed bulletins so 
  that changes or additions can be made to previously sealed 
  bulletins.  In this release of Martus you will only be able 
  to search and view the full contents of the most current 
  version (you will be able to view the title/bulletin-id/
  saved-date of any previous versions that are stored on your 
  computer, by clicking on the Bulletin Details button).  
- Added ability to install new and updated translations at 
  any time following a full Martus release.  A Language pack 
  for each language (including English) can contain the Martus 
  Client User Interface translation, the User Guide, QuickStart 
  Guide, README file, and online help.  Language packs will be 
  made available for download on the Martus website.
- Several changes were made to increase speed when managing 
  bulletins and folders (e.g. sorting, moving bulletins)
- Enhancements to custom field functionality (e.g. grid column 
  sizing)
- Thai translation introduced 
- Arabic translation introduced 
- Changes made to appropriately display Right-to-Left languages 
  (e.g. Arabic)
- Several improvements to display of the Martus Client User 
  Interface in Linux.
- Fixed a problem when retrieving or importing bulletins with very
  large attachments that could cause Martus to exit with an
  "out of memory" error. Any attachment smaller than 20 megabytes
  is unlikely to trigger this error.
- Numerous minor bug fixes, clarifications and clean-ups to the 
  user screens.
- There may be issues with the display of some screens in the 
  Martus 2.5 installation program when using the Nepali or Thai 
  language.  Because it is difficult to test this on all versions 
  of Windows in those languages, we would appreciate any feedback 
  on display of those languages when installing Martus.


Version 2.0.1             2004-08

- Added a horizontal scroll-bar for custom field grids that are 
  wider than the screen width.
- French translation introduced
- Russian and Spanish User documentation updated with 2.0 
  functionality
- Minor clarifications and clean-ups to the English User 
  documentation
- Installer changes to deal with upgrade issues in non-English 
  Windows 98 and ME
- Addition of multi-file (floppy) option on download site


Version 2.0.0             2004-07

- You can now have multiple Headquarter accounts, and there is 
  an enhanced interface to set them up.  This is particularly 
  useful if you want multiple people in your organization to 
  review your bulletins. 
- Headquarters accounts can now send bulletins to a server on 
  behalf of a field desk that lacks internet access.
- Custom Fields functionality has been extended so that you can 
  now create custom fields of different types  (e.g. date, grid, 
  Yes/No).  
- We are using a new open source Windows installer (NSIS) which 
  can be run in non-latin alphabets.  
- A new "Saved Bulletins" folder replaces the Outbox, Sent 
  Bulletins and Draft bulletins folders
- Each bulletin list now displays a column showing whether or 
  not the bulletin was successfully sent to server or not
- The date a bulletin was last saved is now displayed in 
  bulletin preview lists and in the bulletin header
- When creating/modifying a bulletin, the "Send" button has 
  been changed to "Save Sealed" 
- The Martus 1.5 Quick Erase functionality has been replaced by 
  two menu items:  "Delete My Data" which removes this account's 
  bulletins and key pair; and "Delete All Data and Remove Martus" 
  which uninstalls Martus and removes the entire Martus directory 
  including other accounts' data  this is meant to only be used 
  in emergency cases
- Enhanced key backup functionality - you do not have to do a 
  backup before you have created any bulletins, but are reminded 
  to backup your key at a later point if you have not done so.
- For bulletin searching, the user can use either English 'and' 
  and 'or', or the translated equivalents.  This allows users who 
  do not have access to native-language keyboard to still perform 
  'and' and 'or' searches.
- A new "Bulletin Details" button shows a unique bulletin-id and 
  the Headquarters accounts that can view the private data in 
  this bulletin
- When printing, you now have option to hide or include private 
  data 
- Online help is now searchable 
- You can set a flag to disallow public bulletins for security 
  reasons
- Improvements to folder renaming functionality
- Numerous minor bug fixes, clarifications and clean-ups to the 
  user interface.


Version 1.5.0             2004-02

- Multiple accounts can now be set up on a computer.  This 
  changes the login screen in several ways:  You have a choice 
  between signing in to an already setup account, creating a 
  new account, or restoring from a key pair backup.  There will 
  be a sub-directory for each account under the Martus directory.
- The ability to select the language in which you want to run 
  Martus is now available on the signin screen.
- Improved key pair backup functionality, and the ability to back 
  up into multiple "secret share" files to be distributed to friends.  
- Extended Quick Erase functionality to allow removal of key pair, 
  scrubbing of bulletin and key pair data before deletion, and the 
  ability to complete the Quick Erase operation and exit Martus 
  without user-interaction/prompting.
- A new Tools Menu (for Quick Erase, key pair, and HeadQuarters 
  actions)
- Improved communication with Martus backup servers, and clearer 
  messages to user about server status
- Additional language options added to bulletin selection list
- A bug was fixed that prevented previous versions of the Martus 
  program from successfully retrieving bulletins from a backup 
  server if the bulletins contained public information with 
  non-English letters.  This bug did not affect the ability for 
  bulletins to be securely backed up to a Martus server.  The 
  problem would only be noticed if the bulletins were not present 
  on your machine, and you tried to retrieve your own or your 
  field office sealed bulletins containing non-English language 
  public data (the message received in this case was "Errors 
  occurred while retrieving bulletin summaries.  Some of the 
  bulletins on the server will not be shown.")  The bug did not 
  affect the retrieval of all-private or draft bulletins.  You 
  must also be accessing a production Martus backup server, or 
  upgrade your own server to run Martus Server Software version 
  2.0 or later, to be able to retrieve bulletins with non-English 
  language public data.
- Update to Java version to 1.4.2_03
- Numerous minor bug fixes, clarifications and clean-ups to the 
  user interface.


Version 1.2.1             2003-12

- Russian version introduced, including special version of 
  Russian install program.
- English and Spanish User Guides updated with 1.2 functionality
- LinuxJavaInstall.txt script created to simplify Java 
  installation on GNU/Linux machines.


Version 1.2.0             2003-09

- You can now customize the fields of all subsequently created 
  bulletins.  Customizing fields is currently only intended to 
  be used by "advanced" users.  When you choose that menu option, 
  you are warned about this, and told that if you don't know what 
  you're doing, you should turn back or risk messing up your 
  system.  When you do get there, you're provided with a list
  of the existing standard field tags, separated by semi-colons.  
  You can remove any of those, except for four "required" fields: 
  entrydate, language, author, and title.  You can change the 
  sequence.  You can also insert your own new custom fields.  
  Each custom field must have a "tag", which is a string of 
  lower-case ASCII letters, followed by a comma, followed by the 
  prompt that will be shown on screen, which can contain mixed 
  case letters and spaces.  If you try to save a custom field
  definition string that violates any of the rules, you'll be 
  told that it is invalid, but you currently are given no 
  indication of which rule you have broken.
- You now have the option of entering a date range (a time 
  period between two dates), for the bulletin event date.
- Russian UI added, with over 90% of the messages translated 
  into Russian.
- You can now specify an entire folder for export.
- If the normal Martus ports are not available for 
  communication with a backup server because of problems such 
  as firewall configuration issues, the Martus program falls 
  back to using ports 80/443.
- The usability of the keypair backup process has been improved.
- CD image and full download include a newer version of Java, 
  v 1.4.1_03.
- Numerous minor bug fixes, clarifications and clean-ups to the 
  user interface, including more support for mouseless operation.


Version 1.0.3             2003-05

- Release Linux and Mac compatible Martus versions.
- If you have installed a previous version of Martus software, 
  you can download a smaller version of Martus software 
  without having to download the full, larger file containing 
  all of Java.
- Bulletins may be exported from the program in an XML text 
  format.
- Check for compatible Java version during installation and 
  startup.
- Improve speed of manipulating bulletins with attachments.
- Change to busy-cursor during time consuming operations.
- You can now view attachments when running in Windows.
- Changes to the GNU GPL compatible license agreement, relaxing 
  user-notification requirements when borrowing code for uses 
  unrelated to Martus software, and extending coverage to 
  Martus server applications.
- You are now able to receive messages from a Martus server, 
  including a server compliance statement.
- There's a new Welcome dialog that appears when you start 
  the program, indicating whether this is an official version 
  of the program.
- Simplify Martus software installation when downloading the 
  install program. The Jar verification program is now available 
  as a separate download.
- A new Quick Erase feature will delete all local copies of 
  bulletins from your hard drive.
- Fixed a bug where a bulletin could become corrupted if you 
  retrieved it from a server and then modified a copy of it by 
  adding a new attachment.
- Updated some Spanish translation strings.
- Fixed a problem with the window needing the current focus 
  when the user times out.
- Force the signin dialog to be the top window, and add it to 
  the task bar.
- Hide main window during editing.
- Include an updated crypto library, and a new version of Java, 
  v 1.4.1_02, that supports entry of foreign characters using 
  the numeric keypad and fixes a memory leak that gradually 
  depletes available memory.
- Add standard Windows hot keys in obvious places, such as del, 
  and control- C, X, V, and A.


Version 1.0.2             2003-02

- When you choose to modify a sealed bulletin it makes a copy 
  of the bulletin, but if the original had attachments, the 
  system could become confused and end up damaging the copy, 
  the original, or both. This has been fixed.
- Allow CD distribution of default contents that can pre-
  populate the Details field of every bulletin.  If 
  DefaultDetails.txt is on the CD it will be copied to the 
  Martus directory upon installation.  Then, if 
  DefaultDetails.txt is found in the Martus directory when 
  a new account is created, it will give the user the 
  opportunity to use this as their default Details contents 
  for any bulletins they create.
- Reorganize code and internal package structure for easier
  maintainability.


Version 1.0.1             2003-01

- Ask during program installation if the user wants the Martus
  program, the Martus uninstall, and the documentation files 
  added to the Windows Start menu.
- Update from Java Runtime Environment version 1.4.1 to 
  version 1.4.1_01, gaining minor Java security improvements.
- Make sure all required third party sources, third party 
  runtime licenses, documentation files, and Winsock programs 
  for Windows 95, are copied to the hard drive during 
  installation.
- Correct errors in the About Box and the documentation's 
  copyright notice.


