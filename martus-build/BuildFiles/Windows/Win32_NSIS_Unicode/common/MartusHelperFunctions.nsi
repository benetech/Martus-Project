;The Martus(tm) free, social justice documentation and
;monitoring software. Copyright (C) 2001-2004, Beneficent
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

; -----------------------
; GetNextParm - by Dave Laundon
; http://forums.winamp.com/showthread.php?s=96bd8fa4b7d9cedaacf63e427177e9dd&threadid=108983
;   Processes the next parameter, which may be quoted, from the string on the
;   top of the stack.
; Usage:
;   Push <parameters>
;   Call GetNextParm
;   Pop <next parameter>
;   Pop <remaining parameters>
!macro GetNextParm UN
Function ${UN}GetNextParm
  Exch $0
  Push $1
  Push $9
  Push $8
  StrCpy $1 ""

; Trim leading space
TrimLeading:
  StrCpy $9 $0 1
  StrCmp $9 "" Done
  StrCmp $9 '"' DelimitQuote
  StrCmp $9 " " "" DelimitSpace
  StrCpy $0 $0 "" 1
  Goto TrimLeading

; Begin a quote-delimited parameter
DelimitQuote:
  StrCpy $0 $0 "" 1
  Goto CopyParm

; Begin a space-delimited parameter
DelimitSpace:
  StrCpy $9 " "

; Extract the parameter
CopyParm:
  StrCpy $8 $0 1
  StrCmp $8 "" Done
  StrCpy $0 $0 "" 1
  StrCmp $8 $9 Done
  StrCpy $1 $1$8
  Goto CopyParm

Done:
  Pop $8
  Pop $9
  Exch $1
  Exch
  Exch $0
  Exch
FunctionEnd
!macroend
!insertmacro GetNextParm ""
!insertmacro GetNextParm "un."

!macro IsAdmin UN
Function ${UN}IsAdmin
	ClearErrors
	UserInfo::GetName
	IfErrors Win9x
	Pop $0
	UserInfo::GetAccountType
	Pop $1

    StrCmp $1 "Admin" done
    StrCmp $1 "Power" done
    StrCmp $1 "User" user_isnot_admin
    StrCmp $1 "Guest" user_isnot_admin
        MessageBox MB_ICONINFORMATION|MB_OK "$(NeedAdminPrivilegesError_Text)" /SD IDOK
	user_isnot_admin:
        MessageBox MB_ICONINFORMATION|MB_OK "$(NeedAdminPrivileges_Text)" /SD IDOK
        Abort

	Win9x:
		# don't care about admin or not admin in Windows 9x

	done:
FunctionEnd
!macroend
!insertmacro IsAdmin ""
!insertmacro IsAdmin "un."

!macro checkForMartusInstallation UN
Function ${UN}checkForMartusInstallation
     StrCmp $DEBUG_INFO "Y" 0 +2
     MessageBox MB_OK 'Checking for other Martus versions...'
    
     StrCpy $INSTALLER_ACTION ${ACTION_INITIAL_INSTALL}

     ; check if it's an existing installation
     ClearErrors
     EnumRegKey $0 HKLM "Software\Benetech\${PRODUCT_NAME}" 0
     IfErrors check_is_upgrade check_version_compliance

check_is_upgrade:
    StrCmp ${IS_JAVA_DELIVERED} "Y" continue_installation no_martus_present

check_version_compliance:
     StrCmp $DEBUG_INFO "Y" 0 +2
     MessageBox MB_OK 'Registry entry found...'

     ReadRegStr $EXISTING_MARTUS_VERSION HKLM "Software\Benetech\${PRODUCT_NAME}" "CurrentVer"
     StrCmp $EXISTING_MARTUS_VERSION "" remove_previous_InstallShield_martus 0
     StrCmp $EXISTING_MARTUS_VERSION ${PRODUCT_EXTENDED_VERSION} same_version_present 0

     Push $EXISTING_MARTUS_VERSION
     Push ${PRODUCT_EXTENDED_VERSION}
     Call ${UN}VersionCheck
     Pop $0
     StrCmp $0 "2" 0 newer_version_present
     StrCmp $0 "1" 0 older_version_present
     StrCmp $0 "0" 0 same_version_present
     
no_martus_present:
     MessageBox MB_OK "$(CannotUpgradeNoMartus_Text)"  /SD IDOK
     Goto abort_installation

newer_version_present:
     MessageBox MB_OK "$(NewerVersionInstalled_Text)"  /SD IDOK
     Goto abort_installation

same_version_present:
     MessageBox MB_YESNO "$(SameVersionInstalled_Text)" /SD IDNO IDYES 0 IDNO abort_installation
     StrCpy $INSTALLER_ACTION ${ACTION_REINSTALL_SAME}
     Goto continue_installation

older_version_present:
     Push $EXISTING_MARTUS_VERSION
     Push ${PRODUCT_OLDEST_NSIS_UPGRADEABLE_EXTENDED_VERSION}
     Call ${UN}VersionCheck
     Pop $0
     StrCmp $0 "2" 0 check_java
     StrCmp $0 "1" 0 uninstall_manually_and_remove_startmenuitems
     StrCmp $0 "0" 0 check_java

check_java:	 
    StrCmp ${IS_JAVA_DELIVERED} "Y" replace_older_version check_for_ancient_version
    Goto check_for_ancient_version
    
uninstall_manually_and_remove_startmenuitems:
     MessageBox MB_OK "$(UninstallMartusManuallyAndRemoveLinks_Text)"  /SD IDOK
     Goto abort_installation


check_for_ancient_version:
     Push $EXISTING_MARTUS_VERSION
     Push ${PRODUCT_OLDEST_UPGRADEABLE_EXTENDED_VERSION}
     Call ${UN}VersionCheck
     Pop $0
     StrCmp $0 "2" 0 replace_older_version
     StrCmp $0 "1" 0 too_old_to_upgrade
     StrCmp $0 "0" 0 replace_older_version
	 
too_old_to_upgrade:
     MessageBox MB_OK "$(CannotUpgradeNoMartus_Text)"  /SD IDOK
     Goto abort_installation

replace_older_version:
     MessageBox MB_OK "$(UpgradeVersionInstalled_Text)"  /SD IDOK
     StrCpy $INSTALLER_ACTION ${ACTION_UPGRADE_OLDER}
     Goto continue_installation

remove_previous_InstallShield_martus:
    StrCmp ${IS_JAVA_DELIVERED} "Y" 0 notify_full_upgrade_necessary
    Call ${UN}PreviousMartusInstallShieldInstall
    Goto continue_installation
    
notify_full_upgrade_necessary:
    MessageBox MB_OK "$(CannotUpgradeNoJava_Text)"  /SD IDOK

abort_installation:
     Abort
continue_installation:
     StrCmp $DEBUG_INFO "Y" 0 +2
     MessageBox MB_OK 'Exit Other Martus version checks...'
FunctionEnd
!macroend
!insertmacro checkForMartusInstallation ""
!insertmacro checkForMartusInstallation "un."

; --------------------------------------------
; removes previous Martus installshield installation
;
!macro PreviousMartusInstallShieldInstall UN
Function ${UN}PreviousMartusInstallShieldInstall
    IfFileExists "C:\Program Files\InstallShield Installation Information\{0FD93641-8782-11D6-9684-0003474B5EB0}\setup.exe" 0 continue_installation
    MessageBox MB_YESNO "$(RemoveInstallShieldVersion_Text)"  /SD IDYES IDYES 0 IDNO abort_installation
    ExecWait 'RunDll32 C:\PROGRA~1\COMMON~1\INSTAL~1\engine\6\INTEL3~1\ctor.dll,LaunchSetup "C:\Program Files\InstallShield Installation Information\{0FD93641-8782-11D6-9684-0003474B5EB0}\setup.exe"  UNINSTALL'
    IfFileExists "C:\Program Files\InstallShield Installation Information\{0FD93641-8782-11D6-9684-0003474B5EB0}\setup.exe" 0 continue_installation
    
    MessageBox MB_OK "$(CannotRemoveInstallShieldVersion_Text)"  /SD IDOK
abort_installation:
    Abort
continue_installation:
FunctionEnd
!macroend
!insertmacro PreviousMartusInstallShieldInstall ""
!insertmacro PreviousMartusInstallShieldInstall "un."

; ------------------------
; function VersionCheck - by Hendri Adriaens (HendriAdriaens@hotmail.com)
; http://nsis.sourceforge.net/archive/viewpage.php?pageid=57
!macro VersionCheck UN
Function ${UN}VersionCheck
  Exch $0 ;second versionnumber
  Exch
  Exch $1 ;first versionnumber
  Push $R0 ;counter for $0
  Push $R1 ;counter for $1
  Push $3 ;temp char
  Push $4 ;temp string for $0
  Push $5 ;temp string for $1
  StrCpy $R0 "-1"
  StrCpy $R1 "-1"
  Start:
  StrCpy $4 ""
  DotLoop0:
  IntOp $R0 $R0 + 1
  StrCpy $3 $0 1 $R0
  StrCmp $3 "" DotFound0
  StrCmp $3 "." DotFound0
  StrCpy $4 $4$3
  Goto DotLoop0
  DotFound0:
  StrCpy $5 ""
  DotLoop1:
  IntOp $R1 $R1 + 1
  StrCpy $3 $1 1 $R1
  StrCmp $3 "" DotFound1
  StrCmp $3 "." DotFound1
  StrCpy $5 $5$3
  Goto DotLoop1
  DotFound1:
  Strcmp $4 "" 0 Not4
    StrCmp $5 "" Equal
    Goto Ver2Less
  Not4:
  StrCmp $5 "" Ver2More
  IntCmp $4 $5 Start Ver2Less Ver2More
  Equal:
  StrCpy $0 "0"
  Goto Finish
  Ver2Less:
  StrCpy $0 "1"
  Goto Finish
  Ver2More:
  StrCpy $0 "2"
  Finish:
  Pop $5
  Pop $4
  Pop $3
  Pop $R1
  Pop $R0
  Pop $1
  Exch $0
FunctionEnd
!macroend
!insertmacro VersionCheck ""
!insertmacro VersionCheck "un."

; -------------------------------
; function ParseCommandLine - by Hendri Adriaens (HendriAdriaens@hotmail.com)
; http://forums.winamp.com/showthread.php?s=96bd8fa4b7d9cedaacf63e427177e9dd&threadid=66750
!macro ParseCommandLine UN
Function ${UN}ParseCommandLine
; parse the command line
  Push $CMDLINE
  Call ${UN}GetNextParm
  Pop $0 ; exe name
  StrCpy $DEBUG_INFO "N"

ParmsLoop:
  Call ${UN}GetNextParm
  Pop $0
  StrCmp $0 "" ParmsDone ; No more parms
  StrCpy $1 $0 2
  StrCmp $1 "_=" ParmsDone ; No more /useful/ parms
  StrCmp $0 "debug_info" ParmsDebugInfo ; No more parms
  Goto ParmsLoop

ParmsDebugInfo:
  StrCpy $DEBUG_INFO "Y"
  Goto ParmsLoop

ParmsDone:
  Pop $0 ; Tidy the stack
FunctionEnd
!macroend
!insertmacro ParseCommandLine ""
!insertmacro ParseCommandLine "un."
