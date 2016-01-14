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

!define LANG "BURMESE" ; Required
;!insertmacro LANG_STRING <STRING_DEFINE> "string_value"

; language selection dialog stuff
!insertmacro LANG_STRING LangDialog_Title "ဘာသာစကား အင္စေတာ္လာ"
!insertmacro LANG_STRING LangDialog_Text "ေက်းဇူးျပဳၿပီး ဘာသာစကား အင္စေတာ္လာကုိ ေရြးပါ။"

!insertmacro LANG_STRING FinishDialog_Text "${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} ကုိ သင္၏ ကြန္ပ်ဴတာမွာ တပ္ဆင္ထားသည္။\r\n \r\n Martus မြမ္းမံျပင္ဆင္ထားသည့္ ဘာသာစကား အထုပ္ (Language Pack) မ်ား ရွိသလားကုိ ၾကည့္႐ႈရန္ https://www.martus.org/ ကုိ ဝင္ၾကည့္ပါ။\r\n \r\nဘာသာစကား အထုပ္ (Language Pack) က Martus ဗားရွင္း အျပည့္ကုိ ထုတ္ေဝလုိက္သည့္ ေနာက္မွာ အသစ္ျဖစ္ၾကကာ  မြမ္းမံၿပီး ဘာသာျပန္မႈမ်ား သုိ႔မဟုတ္ စာရြက္စာတမ္းကုိ အခ်ိန္မေရြး တပ္ဆင္ခြင့္ကုိ ေပးပါသည္။ ဘာသာစကား အထုပ္ (Language Packs) ထဲမွာ Martus ေဖာက္သည္ သံုးစြဲသူ အင္တာေဖ့ (Client User Interface) ဘာသာျပန္၊ သံုးစြဲသူ လမ္းၫႊန္၊ အျမန္စတင္ေရးလမ္းၫႊန္၊ ဖတ္ရန္ဖုိင္၊ ႏွင့္ ပရုိဂရမ္တြင္းပါ အကူအညီတုိ႔ ပါဝင္ပါသည္။ \r\n \r\nဤ ဝိဇၨာ Wizard ကုိ ပိတ္ရန္ အဆံုးသတ္ (Finish) ကုိ ႏွိပ္ပါ"
!insertmacro LANG_STRING FinishDialog2_Text "."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "သင္၏ Windows Start Menu ထဲမွာ Martus ျဖတ္လမ္း (shortcut) ကုိ သင္ တပ္ဆင္လုိပါသလား ?"
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "သင္၏ ကြန္ပ်ဴတာ မ်က္ႏွာျပင္ ေပၚမွာ Martus ျဖတ္လမ္း (shortcut) ကုိ သင္ တပ္ဆင္လုိပါသလား?"
!insertmacro LANG_STRING LaunchProgramInfo_Text "Martus ျဖတ္လမ္း (shortcut) တစ္ခုကုိ ပရုိဂရမ္ ဖုိင္တြဲ $INSTDIR ထဲကုိတပ္ဆင္ေပးခဲ့သည။ Martus ကုိ စတင္ရန္ ဤျဖတ္လမ္းကုိ သုိ႔မဟုတ္ မိတၱဴတစ္ခုကုိ သံုးပါ။"

!insertmacro LANG_STRING MartusShortcutDescription_Text "Martus လူ႔ရပုိင္ခြင့္မ်ား စာေစာင္ စနစ္"

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "သံုးစြဲသူ လမ္းၫႊန္"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide_bur.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "အျမန္စတင္ေရးလမ္းၫႊန္"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide_bur.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "ျပန္ျဖဳတ္"

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "Martus စာစာင္ မွတ္တမ္း"

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "$(^Name) ကုိ သင္၏ ကြန္ပ်ဴထဲမွ ေအာင္ျမင္စြာ ဖယ္ရွားၿပီးၿပီ။"

!insertmacro LANG_STRING NeedAdminPrivileges_Text "သင္သည္ ေဒသႏၲရ စက္ေပၚမွာ  $(^Name)ကုိ တပ္ဆင္ႏုိင္ရန္အတြက္ အုပ္ခ်ဳပ္ေရး အထူးလုပ္ပုိင္ခြင့္မ်ား ရွိရမည္။"
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "အုပ္ခ်ဳပ္ေရး အထူးလုပ္ပုိင္ခြင့္မ်ားဆုိင္ရာ မသိရသည့္ အမွား။ သင္သည္ ဤစက္ေပၚမွာ အုပ္ခ်ဳပ္ေရး အထူးလုပ္ပုိင္ခြင့္မ်ား ရွိေၾကာင္းကုိ စိစစ္ခံျပပါ၊ သုိ႔မဟုတ္ပါက $(^Name) တပ္ဆင္မႈသည္ ေအာင္ျမင္ခ်င္မွ ေအာင္ျမင္မည္ီ။"

!insertmacro LANG_STRING UninstallProgramRunning_Text "သင္သည္ $(^Name) ထဲမွထြက္သြားၿပီ ျဖစ္ေၾကာင္း စိစစ္ခံျပပါ၊ သုိ႔မဟုတ္ အန္အင္စေတာ္လာသည္ သံုးေနဆဲဖုိင္မ်ားကုိ ဖယ္ရွားႏုိင္မည္ မဟုတ္ပါ။"

!insertmacro LANG_STRING NewerVersionInstalled_Text "${PRODUCT_NAME} ၏ ပုိသစ္ေသာ ဗါးရွင္း ($EXISTING_MARTUS_VERSION) ကုိ တပ္ဆင္ၿပီး ျဖစ္သည္။ သင့္အေနႏွင့္ ပုိေဟာင္းေသာ ဗါးရွင္းကုိ တပ္ဆင္ႏုိင္ရန္အတြက္ ရွိေနဆဲ မိတၱဴကုိ သင္ ျပန္ျဖဳတ္ရန္ လုိသည္။ မည္သုိ႔ပင္ျဖစ္ေစ သင္က အေဟာင္းဆီကုိ ေျပာင္းလွ်င္၊ လုပ္ကုိင္ႏုိင္စြမ္း ဆံုး႐ံႈးသြားႏုိင္ကာ ဗါးရွင္း အသစ္ကုိ သံုးၿပီး ဖန္တီးခဲ့သည့္ စာေစာင္မ်ားကုိ သင္ ၾကည့္႐ႈႏုိင္ခ်င္မွ ၾကည့္႐ႈႏုိင္မည္။ ဗါးရွင္း အသစ္ကုိ ဆက္ထားရွိရန္ OK ကုိ ႏွိပ္လ်က္ တပ္ဆင္မႈထဲမွ ထြက္လုိက္ပါ။ အကယ္၍ လုပ္ကုိင္ႏုိင္စြမ္း ဆံုး႐ံႈးသြားမွာ ျဖစ္သည့္တုိင္ေအာင္၊ သင္က အေဟာင္းဆီကုိ ကူးေျပာင္းလုိေသးသည္ ဆုိလွ်င္၊ လက္ရွိ တပ္ဆင္မႈ ထဲကေနၿပီး ထြက္လုိက္ပါ၊ ပုိသစ္သည့္ ဗါးရွင္းကုိ ျပန္ျဖဳတ္ပါ၊ ထုိ႔ေနာက္မွာ လက္ရွိ ဗါးရွင္း အေဟာင္းကုိ ျပန္ၿပီးတပ္ဆင္ယူပါ။"
!insertmacro LANG_STRING SameVersionInstalled_Text "${PRODUCT_NAME} ၏ လက္ရွိ ဗါးရွင္း ($EXISTING_MARTUS_VERSION) ကုိ တပ္ဆင္ၿပီး ျဖစ္ပါသည္။ သင္သည္ ျပန္ၿပီး တပ္ဆင္လုိပါသလား?"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "${PRODUCT_NAME} ၏ ပုိေဟာင္းေသာ  ဗါးရွင္း ($EXISTING_MARTUS_VERSION) ကုိ တပ္ဆင္ၿပီး ျဖစ္ပါသည္။ အင္စေတာ္လာသည္ ဗါးရွင္း ${PRODUCT_EXTENDED_VERSION} ဆီကုိ ျမႇင့္တင္ေပးမည္။"
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "${PRODUCT_NAME} ကုိ သင္၏ကြန္ပ်ဴတာထဲမွာ တပ္ဆင္ထားလ်က္ ရွိပါသည္။ ကြၽႏ္ုပ္တုိ႔သည္ ၄င္းအတြက္ အန္အင္စေတာ္လာကုိ ဖြင့္သံုးရန္ ႀကိဳးပမ္းမည္ျဖစ္ကာ၊ ၿပီးဆံုးသြားသည့္ႏွင့္ လက္ရွိ တပ္ဆင္မႈကုိ လုပ္ေဆာင္သြားမည္။ သင္သည္ Martus ၏ လက္ရွိ ဗါးရွင္းထဲမွာ ေသာ့ခ်က္ မိတၱဴကူးမႈကုိ မလုပ္ခဲ့ရေသးပါက၊ လက္ရွိ တပ္ဆင္မႈထဲမွ ထြက္လုိက္ကာ ျပန္မျဖဳတ္ခင္တြင္ မိတၱဴကူးယူမႈကုိ ျပဳလုပ္ရန္ မိမိတုိ႔ အၾကံေပးလုိပါသည္။ ထုိ႔ေနာက္မွာ လက္ရွိ အင္စေတာ္လာကုိ ျပန္ၿပီး ဖြင့္သံုးႏုိင္သည္။ သင္သည္ တပ္ဆင္မႈကုိ ဆက္ၿပီး လုပ္ကုိင္လုိပါသလား?"
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "သင္ တပ္ဆင္ထားသည့္ Martus ဗါးရွင္းကုိ  Java တြဲပါလာသည့္ ဗါးရွင္း အျပည့္ျဖင့္သာ အပ္ဂရိတ္ ျပဳလုပ္၍ ရႏုိင္ပါမည္။"
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "သင္၏ ကြန္ပ်ဴတာထဲက Martus ဗါးရွင္း အေဟာင္းကုိ မိမိတုိ႔ ဖယ္ရွား၍ မရႏုိင္ခဲ့ပါ။ ယခုေတာ့ အင္စေတာ္လာသည္ ပိတ္ေတာ့မည္  ျဖစ္ကာ၊ ေက်းဇူးျပဳၿပီး ထိန္းခ်ဳပ္မႈ က႑ (Control Panel) ထဲက ပရုိဂရမ္မ်ား ထည့္/ဖယ္ရွား (Add/Remove Programs) ကုိ သံုးၿပီး သင္၏ Martus မိတၱဴကုိ ဖယ္ရွားပါ၊ ထုိ႔ေနာက္မွာ လက္ရွိ အင္စေတာ္လာကုိ ျပန္ၿပီး ဖြင့္သံုးပါ။ သင္သည္ Martus ၏ လက္ရွိ ဗါးရွင္းထဲမွာ ေသာ့ခ်က္ မိတၱဴကူးမႈကုိ မလုပ္ရေသးပါက  ျပန္မျဖဳတ္ခင္မွာ မိတၱဴကူးယူမႈကုိ ျပဳလုပ္ရန္ မိမိတုိ႔ အၾကံေပးလုိပါသည္။"
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "ဤသည္မွာ Martus ၏အပ္ဂရိတ္္ ဗါးရွင္း ျဖစ္သည္။ ေက်းဇူးျပဳၿပီး Java တြဲပါလာသည့္ ဗါးရွင္းကုိ ေဒါင္းလုဒ္ လုပ္ၿပီး တပ္ဆင္ပါ။"

!insertmacro LANG_STRING UninstallMartusManuallyAndRemoveLinks_Text "သင္သည္ 4.3 ထက္ ပုိေဟာင္းသည့္ Martus ဗါးရွင္းမွေနၿပီး လက္ရွိ ဗါးရွင္း  ဆီကုိ အဆင့္ျမႇင့္မရႏုိင္ပါ။ သင္သည္ သင္၏ ပုိေစာေသာ Martus ဗါးရွင္းကုိ ျဖဳတ္ရန္လုိပါမည္ (Start > Programs > Martus > Uninstall Martus ကုိျဖစ္ေစ၊ Control Panel  ထဲမွာ ပရုိဂရမ္မ်ား ထည့္/ဖယ္ရွား (Add/Remove Programs) ကုိျဖစ္ေစ သံုးၿပီး ျပဳလုပ္ႏုိင္သည္) ။ အကယ္၍ သင္သည္  Windows Vista သုိ႔မဟုတ္ Windows 7 ကုိ အသံုးျပဳေနကာ Start menu ထဲတြင္ Martus ျဖတ္လမ္း ရွိေနလွ်င္၊ သင္သည္  Start > Programs ဆီကုိသြားၿပီး Martus အုပ္စုကုိ ညာဖက္ႏွိပ္ကာ 'Delete' ကုိ ေရြးလုိက္ျခင္းျဖင့္ Martus အုပ္စုကုိ ဖ်က္ရန္ပါ လုိအပ္ႏုိင္သည္။ ထုိသုိ႔မလုပ္လွ်င္၊ သင္က Martus 4.3 ကုိျဖဳတ္သည့္အခါမွာ ထုိလင့္ခ္မ်ား က်န္ရစ္ခဲ့ၾကမည္ျဖစ္ရာ လံုျခံဳေရးအတြက္ ျပႆနာျဖစ္လာႏုိင္သည္။"

