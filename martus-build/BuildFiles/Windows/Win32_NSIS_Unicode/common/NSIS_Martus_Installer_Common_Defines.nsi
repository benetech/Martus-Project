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

; ----------------------
; available installer actions
unicode "true"
!define ACTION_UPGRADE_OLDER    1
!define ACTION_REINSTALL_SAME   2
!define ACTION_INITIAL_INSTALL  3

; MUI 1.67 compatible ------
!include "MUI.nsh"
!include "LogicLib.nsh"

; -------------------------------------
; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"

;------------------------------------------------------
; additional functions to add to MUI
!define MUI_CUSTOMFUNCTION_GUIINIT .onGUIInitPerformChecks
!define MUI_CUSTOMFUNCTION_UNGUIINIT un.onGUIInitPerformChecks

;---------------------------------------------------
; Installer Page - FINISH
;---------------------------------------------------
!define MUI_FINISHPAGE_TEXT                 "$(FinishDialog_Text)"

; ----------------------------------------
; Modern UI pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "..\BuildFiles\combined-license.txt"
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

; language strings macros
!macro LANG_LOAD MACRO_LANG
    !insertmacro MUI_LANGUAGE "${MACRO_LANG}"
    ;  !verbose off
    !include "locallang\${MACRO_LANG}.nsh"
    ;  !verbose on
    !undef LANG
!macroend

; -------------------------------------
; macros to set/unset language strings
!macro LANG_STRING NAME VALUE
    LangString "${NAME}" "${LANG_${LANG}}" "${VALUE}"
!macroend

!macro LANG_UNSTRING NAME VALUE
    !insertmacro LANG_STRING "un.${NAME}" "${VALUE}"
!macroend

; Since NSIS 2.26, the language selection dialog of Modern UI 
; hides languages unsupported by the user's selected codepage 
; by default. Define MUI_LANGDLL_ALLLANGUAGES to override that
!define MUI_LANGDLL_ALLLANGUAGES

; -------------------------------------
; Language files

!insertmacro LANG_LOAD "English"
!insertmacro LANG_LOAD "Russian"
!insertmacro LANG_LOAD "French"
!insertmacro LANG_LOAD "Thai"
!insertmacro LANG_LOAD "Arabic"
!insertmacro LANG_LOAD "Farsi"
!insertmacro LANG_LOAD "Spanish"
;!insertmacro LANG_LOAD "Nepali"
;!insertmacro LANG_LOAD "Bengali"
!insertmacro LANG_LOAD "Khmer"
!insertmacro LANG_LOAD "Burmese"
!insertmacro LANG_LOAD "Vietnamese"
!insertmacro LANG_LOAD "SimpChinese"

; ---------------------------------------
; language selection dialog
!define MUI_LANGDLL_WINDOWTITLE "$(LangDialog_Title)"
!define MUI_LANGDLL_INFO "$(LangDialog_Text)"

;--------------------------------
;Reserve Files

    ;These files should be inserted before other files in the data block
    ;Keep these lines before any File command
    ;Only for solid compression (by default, solid compression is enabled for BZIP2 and LZMA)

    !insertmacro MUI_RESERVEFILE_LANGDLL

; MUI end ------

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
VIProductVersion ${PRODUCT_EXTENDED_VERSION}
VIAddVersionKey ProductName "${PRODUCT_NAME}"
VIAddVersionKey FileVersion "${PRODUCT_EXTENDED_VERSION}"
VIAddVersionKey FileDescription "${PRODUCT_NAME} Installer"
VIAddVersionKey LegalCopyright "${PRODUCT_COPYRIGHT_DATE}"
VIAddVersionKey Language "English"

OutFile "${INSTALLER_EXE_NAME}"
CRCCheck on
InstallDir "C:\Martus"
ShowInstDetails show
ShowUnInstDetails show
BGGradient
Caption ""
SubCaption 0 ""
SubCaption 1 ""
SubCaption 2 ""
SubCaption 3 ""
SubCaption 4 ""
UninstallCaption ""

Var "StartMenuShortcut"
Var "DesktopShortcut"
Var "DefaultUIFile"
Var "DEBUG_INFO"
Var "EXISTING_MARTUS_VERSION"
Var "INSTALLER_ACTION"
Var "MARTUS_INSTALLATION_DIR"
Var "MARTUS_LANGUAGE_CODE"

; ----------------------------------
; include file with helper functions
; must be after all the above defines
!include "common\MartusHelperFunctions.nsi"

