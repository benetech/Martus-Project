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

!define LANG "ARABIC" ; Required
;!insertmacro LANG_STRING <STRING_DEFINE> "string_value"

; language selection dialog stuff
!insertmacro LANG_STRING LangDialog_Title "لغة برنامج التثبيت"
!insertmacro LANG_STRING LangDialog_Text "لطفا قم باختيار لغة برنامج التثبيت."

!insertmacro LANG_STRING FinishDialog_Text "لقد تم تنصيب ${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} على الجهاز.\r\n \r\n Visit https://www.martus.org/ to download Martus Language Packs. \r\n \r\nتسمح لك أية 'حزمة لغوية' بتثبيت ترجمات أو توثيقات جديدة و مُحدّثة في أي وقت يتبع إصدارٍ كامل لبرنامج Martus. قد تحتوي الحزم اللغوية على تحديثات لترجمة واجهة مستخدم برنامج Martus العميل و دليل المستخدم و دليل المرجع المختصر و ملف الملاحظات و تعليمات البرنامج الداخلية. إذا كانت هناك وصلة لحزمة لغوية عربية تُلائم إصدارك من برنامج Martus بالأسفل، لطفا قم بتحميل ملف (Martus-ar.mlp) و ضعه في مجلد تثبيت Martus. و عندما تقوم بتشغيل Martus بعد ذلك، سوف تقوم الحزمة بتحميل أية ترجمات و/ أو تعليمات مُحدّثة، و سوف تضع أية توثيق مُحدّث في مجلد Martus\Docs.\r\n \r\nإضغط إنهاء لإغلاق مرشد الإعداد"
!insertmacro LANG_STRING FinishDialog2_Text "."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text " هل تريد تثبيت وصلة سريعة لبرنامج  Martus  في قائمة البداية في ويندوز؟" 
!insertmacro LANG_STRING DesktopShortcutQuestion_Text " هل تريد تثبيت وصلة سريعة لبرنامج  Martus على سطح مكتبك؟ "
!insertmacro LANG_STRING LaunchProgramInfo_Text "لقد تم تثبيت وصلة سريعة لبرنامج  Martus   في مجلد البرنامج  .$INSTDIR استخدم هذه الوصلة أو نسخة منها لتشغيل Martus. "

!insertmacro LANG_STRING MartusShortcutDescription_Text "إطار عمل Martus لإدارة المعلومات والتقاط البيانات"

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "دليل المستخدم"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide_ar.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "المرجع المختصر"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide_ar.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "إزالة البرنامج"

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "أرشيف نشرات Martus"

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "لقد تمت إزالة $(^Name) من على حاسوبك بنجاح."

!insertmacro LANG_STRING NeedAdminPrivileges_Text "انت في حاجة إلى إمتيازات إدارية على الحاسوب المحلي كي تستطيع تثبيت $(^Name)"
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "توجد مشكلة غير معروفة تعوق الحصول على الامتيازات الإدارية. تأكد أن لك امتيازات إدارية على هذا الحاسوب و إلا فقد لا يتم تثبيت $(^Name) بنجاح"

!insertmacro LANG_STRING UninstallProgramRunning_Text "لطفا تحقق من أنك أغلقت $(^Name)  و إلا فلن يتمكن برنامج الإزالة من حذف الملفات الجاري استخدامها."

!insertmacro LANG_STRING NewerVersionInstalled_Text "توجد نسخة أحدث ($EXISTING_MARTUS_VERSION) من ${PRODUCT_NAME} على الحاسوب بالفعل. يجب عليك أولا أن تقوم بإزالة النسخة الموجودة قبلما تستطيع تثبيت هذا الإصدار القديم. و مع ذلك، فاذا قمت بتثبيت النسخة الأقدم فسوف تفقد بعض الخصائص الوظيفية و قد لا تتمكن من الإطلاع على النشرات التي أُنشئت باستخدام الإصدار الأحدث. لكي تحتفظ بالإصدار الحديث، انقر 'استمر' لإجهاض هذا التثبيت. أما إذا كنت تريد تثبيت الإصدار القديم بغض النظر عن فقدان بعض الخصائص الوظيفية، قم بالخروج من هذا التثبيت ثم إزالة الإصدار الحديث ثم قم بإعادة تثبيت هذا الإصدار القديم."
!insertmacro LANG_STRING SameVersionInstalled_Text "الإصدار الحالي ($EXISTING_MARTUS_VERSION) من ${PRODUCT_NAME} مثبت بالفعل. هل تريد إعادة التثبيت؟"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "يوجد إصدار قديم ($EXISTING_MARTUS_VERSION) من ${PRODUCT_NAME} مثبت بالفعل. سيقوم برنامج التثبيت بتحديثه للإصدار ${PRODUCT_EXTENDED_VERSION}."
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "وجدنا تثبيت ${PRODUCT_NAME}  قديم على هذا الحاسوب. سوف نقوم بتشغيل برنامج الإزالة للتخلص منه، و بمجرد الانتهاء من ذلك سوف تستمر عملية التثبيت الحالية. إذا لم تقم بنسخ شفرة دخولك احتياطيا في هذا الإصدار الحالي من  Martus، نقترح عليك الخروج من هذا التثبيت ثم تقوم بنسخ الشفرة احتياطيا قبل عملية الإزالة. بعدها يمكنك إعادة تشغيل برنامج التثبيت هذا."
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "يمكن فقط تحديث إصدار Martus الذي قمت بتثبيته بواسطة الإصدار الكامل لبرنامج التثبيت و الذي يحتوي على بيئة التشغيل Java."
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text " لم نتمكن من إزالة إصدار Martus القديم من على حاسوبك. سوف ينغلق برنامج التثبيت الآن. لطفا قم بإزالة نسختك من Martus باستخدام خاصية Add/Remove Programs الموجودة بلوحة التحكم    Control Panel ثم قم بإعادة تشغيل برنامج التثبيت هذا. إذا لم تكن قد قمت بنسخ شفرة دخولك احتياطيا في هذا الإصدار من Martus، نقترح عليك القيام بذلك قبل عملية الإزالة. "
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "هذا إصدار ترقية لبرنامج مارتوس. الرجاء تنزيل وتنصيب نسخة برنامج التنصيب الكامل الذي يحمل Java"

!insertmacro LANG_STRING UninstallMartusManuallyAndRemoveLinks_Text "You cannot upgrade from versions of Martus older than 4.3 to the current version.$\r$\n $\r$\nYou need to uninstall your earlier version of Martus (either under Start > Programs > Martus > Uninstall Martus, or using Add/Remove Programs in the Control Panel).$\r$\n $\r$\nIf you are running Windows Vista or Windows 7 and had a Martus shortcut in your Start menu, you may also need to remove the Martus group by going to Start > Programs, right-clicking on the Martus group, and picking 'Delete'.$\r$\nOtherwise those links will remain when you uninstall the current version of Martus, which could be a security issue."
