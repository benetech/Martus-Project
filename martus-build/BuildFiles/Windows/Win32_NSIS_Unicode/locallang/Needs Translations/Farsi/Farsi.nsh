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

!define LANG "FARSI" ; Required
;!insertmacro LANG_STRING <STRING_DEFINE> "string_value"

; language selection dialog stuff
!insertmacro LANG_STRING LangDialog_Title "زبانِ برنامهء نصب کننده"
!insertmacro LANG_STRING LangDialog_Text "زبانِ برنامهء نصب کننده را انتخاب کنيد."

!insertmacro LANG_STRING FinishDialog_Text "${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} بر روي كام?يوتر شما نصب شد.\r\n \r\n Visit https://www.martus.org/downloads/ to download Martus Language Packs. \r\n مجموعه يا «بستهء زبان» (Language Pack)اين امکان را برای شما فراهم می کندکه پس از آماده شدن و به بازارآمدنِ هرنسخهء جديدتری از Martus، بتوانيد هرزمان که بخواهيد ترجمه های تازه تر و مدارکِ راهنمای جديدتر را نيز نصب کنيد. بسته های زبان، مجموعه های مستقلی که به زبانهای مختلفِ دنيا آماده شده اند، حاویِ آخرين نسخهء ترجمهء خودِ برنامهء Martus اند به همراه آخرين نسخه های راهنمای استفاده، راهنمای فوری، فايلِ مشخصات (README)، و راهنمای کمک در داخل خودِ برنامه."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "آيا دل تان مي خواهد علامتِ Martus براي کليک کردن در فهرست شروع(Start) نصب ?ردد؟"
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "آيا دل تان مي خواهد علامتMartus روي صفحهء کام?يوترتان نصب ?ردد؟"
!insertmacro LANG_STRING LaunchProgramInfo_Text "علامتِ Martus در فولدرِ برنامه يعني در$INSTDIR نصب شد. از همين برنامه، يا ک?يِ آن، براي بالابردن Martus استفاده کنيد."

!insertmacro LANG_STRING MartusShortcutDescription_Text "سيستمِ بولتنِ خبريِ حقوقِ بشرMartus"

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "User Guide (in English, no translated version available)"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "Quick Start Guide (in English, no translated version available)"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "حذفِ برنامه"

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "آرشيو بولتن هاي مارتوس Martus"

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "$(^Name) از روي کام?يوتر با موفقيّت ?اک شد."

!insertmacro LANG_STRING NeedAdminPrivileges_Text "شما براي آنکه $(^Name) را نصب کنيد به امتياز مديريّت روي اين کام?يوتر نياز داريد."
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "خطاي ناشناخته در کسبِ امتيازاتِ مديريت. تصديق کنيد که شما مديرِ اين کام?يوتر هستيد ?ون در غير اينصورت نصب $(^Name) ناموفق خواهد بود."

!insertmacro LANG_STRING UninstallProgramRunning_Text "بايد مطمئن شويد که از $(^Name) بيرون آمده باشيد، و?رنه برنامهء ?اک کننده نخواهد توانست فايل هايي که هم اکنون باز هستند را ?اک کند."

!insertmacro LANG_STRING NewerVersionInstalled_Text "نسخهء تازه تري ($EXISTING_MARTUS_VERSION) از برنامهء ${PRODUCT_NAME} از قبل نصب شده است. شما بايد اول نسخهء موجود را ?اک کنيد تا بتوانيد اين نسخهء قديمي تر را نصب کنيد. توجه کنيد ا?ر به نسخهء کهنه تر برمي ?رديد بعضي از کارکردها را از دست خواهيد داد، و ممکن است نتوانيد بولتن هايي که با نسخهء تازه تر تهيه شده اند را ببينيد. براي حفظ نسخهء جديد تر دکمهء 'تأييد' را کليک کنيد که از برنامه خارج تان مي کندو اما ا?ر کماکان نسخهء قديمي تر را مي خواهيد، علارغم از دست دادن کارکردهاي تازه، اول از برنامه خارج شويد، بعد برنامه را حذف کنيد، س?س نسخهء قديمي تر را نصب کنيد."
!insertmacro LANG_STRING SameVersionInstalled_Text "نسخهء تازه تري ($EXISTING_MARTUS_VERSION) از برنامهء ${PRODUCT_NAME} از قبل نصب شده است. آيا مي خواهيد آنرا حذف کنيد؟"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "نسخهء($EXISTING_MARTUS_VERSION) که قديمي تر است از${PRODUCT_NAME} از قبل نصب شده. برنامهء نصب کننده آنرا با نسخهء جديدتر${PRODUCT_EXTENDED_VERSION} عوض خواهد کرد."
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "يک نسخهء قديمي تر از${PRODUCT_NAME} روي کام?يوتر شما موجود است. ما سعي خواهيم کرد با استفاده از برنامهء حذف کننده آنرا ?اک کنيم، ?س از حذفِ برنامهء قديمي، نصبِ برنامهء جديد ادامه خواهد يافت. ا?ر شما تاکنون از فايلِ کليدِ در برنامهء موجودِ Martus هنوز نسخهء اضافي تهيه نکرده ايد، ما ?يشنهاد مي کنيم از اين برنامه خارج شويد و نسخهء اضافي را تهيه کنيد و س?س بر?رديد و برنامهء جديد را نصب کنيد. ?س از اين کار شما دوباره مي توانيد برنامهء نصب کننده را به راه اندازيد. آيا حالا مي خواهيد به نصب ادامه دهيد؟"
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "نسخه اي ازMartus را که شما هم اکنون نصب کرده ايد تنها زماني مي تواند جديدتر شود که از قبل نسخهء کامل برنامه نصب کننده به همراه Java(جاوا) ?ياده شده باشد."
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "متأسفانه ما نتوانستيم نسخهء قديميِMartus را از روي کام?يوترِ شما حذف کنيم. برنامهء نصب کننده حالا خروج مي کند، شما خودتان براي حذف برنامهء Martus به قسمت Add/Remove(اضافه/حذف) در Control Panel(صفحهء کنترل) برويد و برنامه را از آنجا حذف کنيد، بعد دوباره به اين برنامهء نصب کننده بر?رديد. ا?ر هنور نسخهء اضافي از کليدتان را در نسخهء فعليMartus تهيه نکرده ايد ?يشنهاد مي کنين هم اکنون اين کار را ?يش از ?اک کردن برنامه بکنيد."
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "اين جديدترين نسخهء برنامهء Martus است. خواهش مي کنيم نسخهء کاملِ برنامهء نصب کننده (installer) را که حامل برنامهء جاوا (Java) است را ?ياده و نصب کنيد."

!insertmacro LANG_STRING UninstallMartusManuallyAndRemoveLinks_Text "You cannot upgrade from versions of Martus older than 4.3 to the current version.$\r$\n $\r$\nYou need to uninstall your earlier version of Martus (either under Start > Programs > Martus > Uninstall Martus, or using Add/Remove Programs in the Control Panel).$\r$\n $\r$\nIf you are running Windows Vista or Windows 7 and had a Martus shortcut in your Start menu, you may also need to remove the Martus group by going to Start > Programs, right-clicking on the Martus group, and picking 'Delete'.$\r$\nOtherwise those links will remain when you uninstall the current version of Martus, which could be a security issue."
