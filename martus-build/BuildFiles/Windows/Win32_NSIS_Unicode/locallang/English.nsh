;The Martus(tm) free, social justice documentation and
;monitoring software. Copyright (C) 2001-2006, Beneficent
;Technology, Inc. (Benetech).

;Martus is free software; you can redistribute it and/or
;modify it under the terms of the GNU General Public License
;as published by the Free Software Foundation; either
;version 2 of the License, or (at your option) any later
;version with the additions and exceptions described in the
;accompanying Martus license file entitled "license.txt".

;It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
;IMPLIED, including warranties of fitness of purpose or
;merchantability.  See the accompanying Martus License and
;GPL license for more details on the required license terms
;for this software.

;You should have received a copy of the GNU General Public
;License along with this program; if not, write to the Free
;Software Foundation, Inc., 59 Temple Place - Suite 330,
;Boston, MA 02111-1307, USA.

!define LANG "ENGLISH" ; Required
;!insertmacro LANG_STRING <STRING_DEFINE> "string_value"

; language selection dialog stuff
!insertmacro LANG_STRING LangDialog_Title "Installer Language"
!insertmacro LANG_STRING LangDialog_Text "Please select the installer language."

!insertmacro LANG_STRING FinishDialog_Text "${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} has been installed on your computer.\r\n \r\n Visit https://www.martus.org/ to see if any updated Martus Language Packs are available. \r\n \r\nA Language Pack allows you to install new and updated translations or documentation at any time following a full Martus release. Language Packs can contain updates to the Martus Client User Interface translation, the User Guide, Quick Start Guide, README file, and in-program help.\r\n \r\nClick Finish to close this Wizard"
!insertmacro LANG_STRING FinishDialog2_Text "."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "Do you need a Martus shortcut installed in your Windows Start Menu?"
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "Do you need a Martus shortcut installed on your Desktop?"
!insertmacro LANG_STRING LaunchProgramInfo_Text "A Martus shortcut was installed in the program folder $INSTDIR. Use this shortcut, or a copy, to launch Martus."

!insertmacro LANG_STRING MartusShortcutDescription_Text "Martus Information Management and Data Collection Framework"

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "User Guide"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "Quickstart"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "Uninstall"

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "Martus Bulletin Archive"

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "$(^Name) was successfully removed from your computer."

!insertmacro LANG_STRING NeedAdminPrivileges_Text "You need administrative privileges on the local machine to be able to install $(^Name)."
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "Unknown error getting admin privileges. Verify you have administrative privileges on this machine otherwise the $(^Name) installation might not be successful."

!insertmacro LANG_STRING UninstallProgramRunning_Text "Please verify that you have exited $(^Name) otherwise the uninstaller will not be able to remove files that are in use."

!insertmacro LANG_STRING NewerVersionInstalled_Text "A newer version ($EXISTING_MARTUS_VERSION) of ${PRODUCT_NAME} is already installed.  You must first uninstall the existing copy before you can install this older version. However if you downgrade, you will lose functionality, and may not be able to view bulletins that were created using the newer version.  To keep the newer version, press OK to exit this installation.  If you still want to downgrade despite loss of functionality, exit this installation, uninstall the newer version, and then re-install this older version."
!insertmacro LANG_STRING SameVersionInstalled_Text "The current version ($EXISTING_MARTUS_VERSION) of ${PRODUCT_NAME} is already installed. Do you wish to reinstall?"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "An older version ($EXISTING_MARTUS_VERSION) of ${PRODUCT_NAME} is installed.  The installer will upgrade it to version ${PRODUCT_EXTENDED_VERSION}."
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "An earlier ${PRODUCT_NAME} installation exists in your computer. We will attempt to launch the uninstaller for it, and once it has completed, the current installation will proceed.  If you have not done a key backup in your current version of Martus, we suggest you exit this installation and do a backup before uninstalling.  You can then rerun this installer. Do you wish to continue with the installation?"
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "The version of Martus you have installed can only be upgraded by the full version installer that carries Java."
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "We were unable to remove the older version of Martus in your computer. The installer will now exit, please remove your copy of Martus using Add/Remove Programs in the Control Panel, and then rerun this installer.  If you have not done a key backup in your current version of Martus, we suggest you do so before uninstalling."
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "This is an upgrade version of Martus. Please download and install the full version installer that carries Java."

!insertmacro LANG_STRING UninstallMartusManuallyAndRemoveLinks_Text "You cannot upgrade from versions of Martus older than 4.3 to the current version.$\r$\n $\r$\nYou need to uninstall your earlier version of Martus (either under Start > Programs > Martus > Uninstall Martus, or using Add/Remove Programs in the Control Panel).$\r$\n $\r$\nIf you are running Windows Vista or Windows 7 and had a Martus shortcut in your Start menu, you may also need to remove the Martus group by going to Start > Programs, right-clicking on the Martus group, and picking 'Delete'.$\r$\nOtherwise those links will remain when you uninstall the current version of Martus, which could be a security issue."
