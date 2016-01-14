
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

!define LANG "KHMER" ; Required

;!insertmacro LANG_STRING <STRING_DEFINE> "string_value"

!insertmacro LANG_STRING LangDialog_Title "ភាសា​សម្រាប់​កម្មវិធី​ដំឡើង"
!insertmacro LANG_STRING LangDialog_Text "សូម​ជ្រើសភាសា​សម្រាប់​កម្មវិធី​ដំឡើង"

!insertmacro LANG_STRING FinishDialog_Text "${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} has been installed on your computer.\r\n \r\n Visit https://www.martus.org/downloads/ to see if any updated Martus Language Packs are available. \r\n \r\nA (Language Pack) allows you to install new and updated translations or documentation at any time following a full Martus release. Language Packs can contain updates to the Martus Client User Interface translation, the User Guide, Quick Start Guide, README file, and in-program help.\r\n \r\nClick Finish to close this Wizard"
!insertmacro LANG_STRING FinishDialog2_Text "."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "តើ​អ្នក​ចង់​​ដំឡើង​ផ្លូវ​កាត់​របស់​ម៉ាថឹស​ទៅ​ក្នុង​ម៉ឺនុយ​ចាប់ផ្តើម​នៃ Windows ​ឬ​ទេ ?"

!insertmacro LANG_STRING DesktopShortcutQuestion_Text "តើ​អ្នក​ចង់ដំឡើង​​ផ្លូវ​កាត់​របស់ Martus ទៅ​លើ​ផៃ្ទ​តុ​នៃ​កុំព្យូទ័រ​ឬ​ទេ ?"
!insertmacro LANG_STRING LaunchProgramInfo_Text "ផ្លូវ​កាត់​របស់ Martus ត្រូវ​បានដំឡើង​ទៅ​ក្នុង​ថត​កម្មវិធី​ឈ្មោះ $INSTDIR ។ សូម​ប្រើ​ផ្លូវ​កាត់​នេះ ឬឈ្មោះ​ចម្លង ដើម្បី​ចាប់ដំណើរ​ការ​ម៉ាថឹស ។" 
!insertmacro LANG_STRING MartusShortcutDescription_Text "ប្រព័ន្ធ​របាយការណ៏​សិទ្ធិ​មនុស្ស​នៃ Martus"

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "ឯកសារ​ណែនាំ​អ្នក​ប្រើប្រាស់"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide_km.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "ឯកសារ​ណែនាំ​អោយចាប់​ផ្តើម​បាន​ឆាប់​រហ័ស"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide_km.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "លុប​កម្មវិធី"

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "ប័ណ្ណសារ​របាយការណ៏​នៃ Martus"

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "$(^Name) ត្រូវ​បាន​យក​ចេញ​ពី​កុំព្យូទ័រ​របស់​អ្នក​ដោយ​ជោគជ័យ។"

!insertmacro LANG_STRING NeedAdminPrivileges_Text "អ្នក​ត្រូវ​មាន​សិទ្ធិ​ជា​អ្នក​គ្រប់​គ្រង​លើ​កុំព្យូទ័រ​នេះ ដើម្បី​ដំឡើង $(^Name) ។"
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "កំហុស​ដែល​មិនស្គាល់ ក្នុង​ការ​យក​សិទ្ធិ​ជា​អ្នក​គ្រប់​គ្រង ។ ផ្ទៀងផ្ទាត់​ថា​អ្នក​មាន​សិទ្ធិ​ជា​អ្នក​គ្រប់​គ្រង​លើ​កុំព្យូទ័រ​នេះ មិន​ដូច្នេះ​ទេ អ្នក​មិន​អាច ដំឡើង $(^Name) ដោយ​ជោគជ័យ​បាន​ទេ ។"

!insertmacro LANG_STRING UninstallProgramRunning_Text "សូម​ផ្ទៀងផ្ទាត់​ថា​អ្នក​ពិត​ជា​បាន​ចាក​ចេញពី $(^Name) មិន​ដូច្នេះ​ទេ អ្នក​មិន​អាច​លុប​សំណុំ​ឯកសារដែល​កំពុង​ប្រើ​ប្រាស់​បាន​ទេ ។" 
!insertmacro LANG_STRING NewerVersionInstalled_Text "កំណែ​ថ្មី ($EXISTING_MARTUS_VERSION) នៃ ${PRODUCT_NAME} ត្រូវ​បាន​ដំឡើង​រូច​ហើយ។​ អ្នក​ត្រូវ​តែ​លុប​កំណែ​ថ្មី​ចេញ​ជា​មុន​សិន​ ទើប​អ្នក​អាច​ដំឡើងកំណែ​ចាស់​នេះ​បាន។​ ប៉ុន្តែ​ បើ​អ្នក​ចង់ដំឡើង​កំណែ​ចាស់ឡើង​វិញ​ អ្នក​នឹង​បាត់​បង់​នូវ​មុខងារ​ខ្លះៗ​ ហើយប្រហែល​ជា​មិន​អាច​មើល​សំណុំ​រឿង​ណា​ដែល​ត្រូវ​បាន​បង្កើត​ដោយ​កំណែ​ថ្មីឡើយ​។​ ដើម្បី​រក្សា​កំណែ​ថ្មី​អោយ​នៅ​ដដែល​ សូម​ចុច​ 'យល់ព្រម' ដើម្បី​ចាក​ចេញ​ពី​ការដំឡើង​​នេះ។​ បើ​អ្នក​នៅ​តែ​ចង់​ដំឡើង​កំណែ​ចាស់​ ទោះ​បី​ជាមាន​ការ​បាត់​បង់​មុខងារ​ខ្លះៗ​ក៏​ដោយ​នោះ​ សូម​ចាកចេញ​ពី​ការ​ដំឡើង​​​នេះ​ រួចលុប​កំណែ​ថ្មី​ចេញ​ ហើយ​ចាប់​ផ្តើម​ការ​ដំឡើង​កំណែ​ចាស់​ម្តង​ទៀត។"
!insertmacro LANG_STRING SameVersionInstalled_Text "កំណែ​បច្ចុប្បន្ន ($EXISTING_MARTUS_VERSION) នៃ ${PRODUCT_NAME} ត្រូវ​បានដំឡើង​​​រូច​ហើយ។ តើ​អ្នក​ចង់ដំឡើង​​​ម្តង​ទៀត​ឬ​ទេ ?"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "កំណែ​ចាស់ ($EXISTING_MARTUS_VERSION) នៃ​${PRODUCT_NAME} ត្រូវ​បាន​ដំឡើង​។ អ្នក​ដំឡើង​​​នឹង​អភិវឌ្ឍ​កំណែ​នេះ​អោយ​ទៅ ${PRODUCT_EXTENDED_VERSION} ។" 
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "​ការ​ដំឡើង​​​កំណែ​មុន​នៃ ${PRODUCT_NAME} មាន​នៅក្នុង​កុំព្យូទ័រ​របស់​អ្នក​រួច​ហើយ។ យើង​នឹង​ប៉ុនប៉ង​ចាប់​ផ្តើម​លុប​វា​ចេញ ហើយ​ពេល​ដែល​លុប​វា​រួច ការ​ដំឡើង​បច្ចុប្បន្ន​នឹង​បន្ត។ បើ​អ្នក​ពុំ​ទាន់​បានចម្លង​កំណែ​បច្ចុប្បន្ន​របស់ Martus ទុក​ជា​ព័ត៌មាន​បម្រុងទេ​ យើង​ស្នើ​អោយ​អ្នក​ចាក​ចេញ​ពី​ការ​ដំឡើង​នេះ​ ហើយ​ចម្លង​ជា​ព័ត៌មានបម្រុង​ ​មុន​ពេល​លុប​វា​ចេញ។​ អ្នកអាច​ដំណើរ​ការដំឡើង​​នេះ​ម្តង​ទៀត​បាន។" 
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "កំណែ​របស់ Martus ដែល​អ្នក​បាន​ដំឡើង​​រួច​ហើយ​នេះ​ ​អាច​ត្រូវ​បាន​អភិវឌ្ឍ​ដោយ​កម្មវិធីដំឡើង​​នៃ​កំណែ​ចុង​ក្រោយ​ដែល​មាន​រួម​បញ្ចូល​កម្មវិធី​ Java។"
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "យើង​មិន​អាច​យក​កំណែ​ចាស់​នៃ Martus ចេញ​ពី​កុំព្យូទ័រ​របស់​អ្នក​បាន​ទេ។​ អ្នក​ដំឡើង ​នឹង​ចាក​ចេញ​ពេល​នេះ​ហើយ​ សូម​យក​ច្បាប់​ចម្លង​នៃ Martus ចេញ​ដោយ​ប្រើ​ប្រាស់​ Add/Remove Programs ក្នុង Control Panel ហើយ​ដំណើរ​ការ​ដំឡើង​នេះ​ម្តង​ទៀត។​ បើ​អ្នក​ពុំ​ទាន់​បាន​ចម្លង​កំណែ​បច្ចុប្បន្ន​របស់ Martus ទុកជា​ព័ត៌មាន​បម្រុង​ទេ​ យើង​ស្នើ​អោយ​អ្នក​​ចម្លង​​វា​ទុក​ជា​មុន​សិន ​មុន​ពេល​លុប​វា​ចេញ។"
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "This is an upgrade version of Martus. Please download and install the full version installer that carries Java."

!insertmacro LANG_STRING UninstallMartusManuallyAndRemoveLinks_Text "You cannot upgrade from versions of Martus older than 4.3 to the current version.$\r$\n $\r$\nYou need to uninstall your earlier version of Martus (either under Start > Programs > Martus > Uninstall Martus, or using Add/Remove Programs in the Control Panel).$\r$\n $\r$\nIf you are running Windows Vista or Windows 7 and had a Martus shortcut in your Start menu, you may also need to remove the Martus group by going to Start > Programs, right-clicking on the Martus group, and picking 'Delete'.$\r$\nOtherwise those links will remain when you uninstall the current version of Martus, which could be a security issue."
