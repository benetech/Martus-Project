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

;--------------------------------
; post install

Section -Post
    WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
    WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$MARTUS_INSTALLATION_DIR\bin\uninst.exe"
    WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
    WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayIcon" "$MARTUS_INSTALLATION_DIR\app.ico"
    WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
    WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
    WriteRegDWORD ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "NoModify" 1
    WriteRegDWORD ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "NoRepair" 1
    WriteUninstaller "$MARTUS_INSTALLATION_DIR\bin\uninst.exe"
SectionEnd

;--------------------------------
; uninstall success
Function un.onUninstSuccess
    HideWindow
    MessageBox MB_ICONINFORMATION|MB_OK "$(UninstallSuccess_Text)" /SD IDOK
FunctionEnd

;--------------------------------
; uninstallation
Section Uninstall
    SetShellVarContext all
    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Checking if program is running...'
    IfFileExists "$MARTUS_INSTALLATION_DIR\lock" 0 martus_not_running
    MessageBox MB_ICONINFORMATION|MB_OK "$(UninstallProgramRunning_Text)" /SD IDOK

martus_not_running:

    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Removing shortcuts...'
    Delete "$DESKTOP\Martus.lnk"
    RMDir /r "$SMPROGRAMS\Martus"

    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Remove registry entries...'
    DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"

    DeleteRegKey HKLM "Software\Benetech\${PRODUCT_NAME}\${PRODUCT_EXTENDED_VERSION}"
    DeleteRegValue HKLM "Software\Benetech\${PRODUCT_NAME}" "CurrentVer"
    DeleteRegKey /ifempty "HKLM" "Software\Benetech\${PRODUCT_NAME}"
    DeleteRegKey /ifempty "HKLM" "Software\Benetech"

	; This should not be necessary, but the start menu entries are not being
	; deleted under Vista/Win7, so we'll try explicitly deleting them (TT 4588)    
    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Deleting previous start menu entries...'
    RMDir /r "$SMPROGRAMS\Martus"

    ReadRegStr $8 HKCR ".mba" ""
    StrCmp $8 "martusfile" 0 skip_mba_deletion
    DeleteRegKey HKCR ".mba"
skip_mba_deletion:
    DeleteRegKey HKCR "martusfile"

    ; removal of program files
    StrCmp $DEBUG_INFO "Y" 0 +2
    MessageBox MB_OK 'Remove program files...'
    IfFileExists "$MARTUS_INSTALLATION_DIR\$$$$$$silent" 0 normal_martus_uninstall
    RMDir /r "$MARTUS_INSTALLATION_DIR"

normal_martus_uninstall:
    Delete "$MARTUS_INSTALLATION_DIR\bin\uninst.exe"
    Delete "$MARTUS_INSTALLATION_DIR\*.ico"
    Delete "$MARTUS_INSTALLATION_DIR\README*.txt"
    Delete "$MARTUS_INSTALLATION_DIR\gpl.txt"
    Delete "$MARTUS_INSTALLATION_DIR\license.txt"
    Delete "$MARTUS_INSTALLATION_DIR\DefaultUI.txt"
    Delete "$MARTUS_INSTALLATION_DIR\DefaultDetails.txt"
    Delete "$MARTUS_INSTALLATION_DIR\autorun.inf"
    Delete "$MARTUS_INSTALLATION_DIR\Martus.lnk"
    Delete "$MARTUS_INSTALLATION_DIR\*.zip"
    RMDir /r "$MARTUS_INSTALLATION_DIR\src"
    RMDir /r "$MARTUS_INSTALLATION_DIR\Docs"
    RMDir /r "$MARTUS_INSTALLATION_DIR\CVS"
    Delete "$MARTUS_INSTALLATION_DIR\verify\readme*.txt"
    Delete "$MARTUS_INSTALLATION_DIR\verify\ssmartusclientks"
    Delete "$MARTUS_INSTALLATION_DIR\verify\*.bat"
    Delete "$MARTUS_INSTALLATION_DIR\verify\JarVerifier.*"
    RMDir "$MARTUS_INSTALLATION_DIR\verify"

    Delete /REBOOTOK "$MARTUS_INSTALLATION_DIR\Martus.jar"
    Delete /REBOOTOK "$MARTUS_INSTALLATION_DIR\infinitemonkey.dll"
    Delete "$MARTUS_INSTALLATION_DIR\bin\Martus Uninstall Silent.bat"
    RMDir /r "$MARTUS_INSTALLATION_DIR\bin"
    RMDir /r "$MARTUS_INSTALLATION_DIR\lib"
    RMDir "$MARTUS_INSTALLATION_DIR"

    SetAutoClose true
SectionEnd

;--------------------------------
; installation initialization
Function .oninit
    ;language selection dialog
    !insertmacro MUI_LANGDLL_DISPLAY

    Call ParseCommandLine
FunctionEnd

;--------------------------------
; uninstallation initialization
Function un.onInit
     ; installation location comes from the registry
     ReadRegStr $MARTUS_INSTALLATION_DIR HKLM "Software\Benetech\${PRODUCT_NAME}\${PRODUCT_EXTENDED_VERSION}" "InstallationDir"
     StrCpy $INSTDIR $MARTUS_INSTALLATION_DIR

     IfFileExists "$MARTUS_INSTALLATION_DIR\$$$$$$silent" 0 visible_martus_uninstall
     SetSilent silent

visible_martus_uninstall:
    ; get uninstallation language
    !insertmacro MUI_LANGDLL_DISPLAY

    Call un.ParseCommandLine
FunctionEnd

Function .onGUIInitPerformChecks
    Call IsAdmin
    Call checkForMartusInstallation
FunctionEnd

Function un.onGUIInitPerformChecks
    Call un.IsAdmin
FunctionEnd

