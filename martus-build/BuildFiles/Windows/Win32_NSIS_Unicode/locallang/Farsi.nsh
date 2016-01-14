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

!insertmacro LANG_STRING FinishDialog_Text "${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} بر روي كامپيوتر شما نصب شد.\r\n \r\n Visit https://www.martus.org/ to download Martus Language Packs. \r\n مجموعه يا «بستهء زبان» (Language Pack)اين امکان را برای شما فراهم می کندکه پس از آماده شدن و به بازارآمدنِ هرنسخه جديدتری از Martus، بتوانيد هرزمان که بخواهيد ترجمه های تازه تر و مدارکِ راهنمای جديدتر را نيز نصب کنيد. بسته های زبان، مجموعه های مستقلی که به زبانهای مختلفِ دنيا آماده شده اند، حاویِ آخرين نسخه ترجمهء خودِ برنامهء Martus اند به همراه آخرين نسخه های راهنمای استفاده، راهنمای فوری، فايلِ مشخصات (README)، و راهنمای کمک در داخل خودِ برنامه."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "آيا دلتان مي خواهد علامتِ Martus براي کليک کردن در فهرست شروع(Start) نصب گردد؟"
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "آيا دلتان مي خواهد علامتMartus روي صفحهء كامپيوترتان نصب گردد؟"
!insertmacro LANG_STRING LaunchProgramInfo_Text "علامتِ Martus در فولدرِ برنامه يعني در$INSTDIR نصب شد. از همين برنامه، يا کپيِ آن، براي بالابردن Martus استفاده کنيد."

!insertmacro LANG_STRING MartusShortcutDescription_Text "سيستمِ بولتنِ خبريِ حقوقِ بشرMartus"

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "User Guide (in English, no translated version available)"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "Quick Start Guide (in English, no translated version available)"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "حذفِ برنامه"

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "آرشيو بولتن هاي مارتوس Martus"

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "$(^Name) از روي كامپيوتر با موفقيّت پاک شد."

!insertmacro LANG_STRING NeedAdminPrivileges_Text "شما براي آنکه $(^Name) را نصب کنيد به امتياز مديريّت روي اين كامپيوتر نياز داريد."
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "خطاي ناشناخته در کسبِ امتيازاتِ مديريت. تصديق کنيد که شما مديرِ اين كامپيوتر هستيد چون در غير اينصورت نصب $(^Name) ناموفق خواهد بود."

!insertmacro LANG_STRING UninstallProgramRunning_Text "بايد مطمئن شويد که از $(^Name) بيرون آمده باشيد، و گرنه برنامهء چاک کننده نخواهد توانست فايل هايي که هم اکنون باز هستند را چاک کند."

!insertmacro LANG_STRING NewerVersionInstalled_Text "نسخه تازه تري ($EXISTING_MARTUS_VERSION) از برنامهء ${PRODUCT_NAME} از قبل نصب شده است. شما بايد اول نسخه موجود را پاک کنيد تا بتوانيد اين نسخه قديمي تر را نصب کنيد. توجه کنيد اگر به نسخه کهنه تر برمي گرديد بعضي از کارکردها را از دست خواهيد داد، و ممکن است نتوانيد بولتن هايي که با نسخه تازه تر تهيه شده اند را ببينيد. براي حفظ نسخه جديد تر دکمهء 'تأييد' را کليک کنيد که از برنامه خارج تان مي کندو اما اگر کماکان نسخه قديمي تر را مي خواهيد، علارغم از دست دادن کارکردهاي تازه، اول از برنامه خارج شويد، بعد برنامه را حذف کنيد، سپس نسخه قديمي تر را نصب کنيد."
!insertmacro LANG_STRING SameVersionInstalled_Text "نسخه تازه تري ($EXISTING_MARTUS_VERSION) از برنامهء ${PRODUCT_NAME} از قبل نصب شده است. آيا مي خواهيد آنرا حذف کنيد؟"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "نسخه($EXISTING_MARTUS_VERSION) که قديمي تر است از${PRODUCT_NAME} از قبل نصب شده. برنامهء نصب کننده آنرا با نسخه جديدتر${PRODUCT_EXTENDED_VERSION} عوض خواهد کرد."
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "يک نسخه قديمي تر از${PRODUCT_NAME} روي كامپيوتر شما موجود است. ما سعي خواهيم کرد با استفاده از برنامهء حذف کننده آنرا پاک کنيم، پس از حذفِ برنامهء قديمي، نصبِ برنامهء جديد ادامه خواهد يافت. اگر شما تاکنون از فايلِ کليدِ در برنامهء موجودِ Martus هنوز نسخه اضافي تهيه نکرده ايد، ما پيشنهاد مي کنيم از اين برنامه خارج شويد و نسخه اضافي را تهيه کنيد و سپس برگرديد و برنامهء جديد را نصب کنيد. پس از اين کار شما دوباره مي توانيد برنامهء نصب کننده را به راه اندازيد. آيا حالا مي خواهيد به نصب ادامه دهيد؟"
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "نسخه اي ازMartus را که شما هم اکنون نصب کرده ايد تنها زماني مي تواند جديدتر شود که از قبل نسخه کامل برنامه نصب کننده به همراه Java(جاوا) پياده شده باشد."
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "متأسفانه ما نتوانستيم نسخه قديميِMartus را از روي كامپيوترِ شما حذف کنيم. برنامهء نصب کننده حالا خروج مي کند، شما خودتان براي حذف برنامهء Martus به قسمت Add/Remove(اضافه/حذف) در Control Panel(صفحهء کنترل) برويد و برنامه را از آنجا حذف کنيد، بعد دوباره به اين برنامهء نصب کننده برگرديد. اگر هنور نسخه اضافي از کليدتان را در نسخه فعليMartus تهيه نکرده ايد پيشنهاد مي کنين هم اکنون اين کار را پيش از پاک کردن برنامه بکنيد."
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "اين جديدترين نسخه برنامهء Martus است. خواهش مي کنيم نسخه کاملِ برنامهء نصب کننده (installer) را که حامل برنامهء جاوا (Java) است را پياده و نصب کنيد."

!insertmacro LANG_STRING UninstallMartusManuallyAndRemoveLinks_Text "در صورتی که شما از نسخه های قدیمی تر از 4.3 استفاده میکنید، قادر به ارتقا به نسخه جدید Martus نخواهید بود. شما باید ابتدا نسخه قدیمی Martus را حذف کنید.$\r$\n $\r$\n(یا از طریق: Start>Programs>Martus>Uninstall Martus و یا با استفاده از برنامه Add/Remove درکنترل پانل اپراتور دستگاه شما).$\r$\n $\r$\nاگر شما از ویندوز ویستا یا ویندوز 7 استفاده میکنید و آیکون Martus را در نوار Start خود قرار داده اید، در این صورت به احتمال زیاد نیاز خواهید داشت تا گروه مربوط به Martus  را نیز حذف کنید. (Start>Programs روی گروه Martus  کلیک سمت راست بر روی ماوس خود را فشار دهید و گزینه Delete را انتخاب کنید.)$\r$\n.در غیر اینصورت این لینکها حتی پس از حذف Martus بر روی ویندوز شما باقی خواهند ماند و ممکن است مشکلات امنیتی را در پی داشته باشد. "
