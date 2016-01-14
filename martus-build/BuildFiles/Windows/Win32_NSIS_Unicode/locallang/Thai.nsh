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

!define LANG "THAI" ; Required
;!insertmacro LANG_STRING <STRING_DEFINE> "string_value"
!insertmacro LANG_STRING LangDialog_Title "ภาษาตัวติดตั้ง"
!insertmacro LANG_STRING LangDialog_Text "โปรดเลือกภาษาตัวติดตั้ง"

!insertmacro LANG_STRING FinishDialog_Text "${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} ถูกติดตั้งเรียบร้อยแล้ว.\r\n \r\n Visit https://www.martus.org/ to see if any updated Martus Language Packs are available. \r\n \r\ชุดภาษา (Language Pack) ยอมให้คุณติดตั้งการแปลภาษาที่ใหม่และอัปเดต หรือเอกสารทุกเวลาตามรุ่นของโปรแกรม Martus ชุดภาษา (Language Pack) รวบรวมการแปลภาษาในส่วนการใช้งานของโปรแกรมลูกข่าย คู่มือการใช้งาน คู่มือฉบับย่อ ไพล์ README และการช่วยเหลือ ของโปรแกรม Martus ให้อัปเดต ถ้าลิ้งค์ชุดภาษาไทยของโปรแกรม Martus รุ่นที่คุณใช้งานปรากฎอยู่ด้านล่าง กรุณาดาวน์โหลดไฟล์ (Martus-th.mlp) และวางที่ไดเร็กทรอรี่ Martus เมื่อคุณเปิดโปรแกรม Martus ครั้งต่อไปมันจะอัปเดตการแปล และ/หรือ การช่วยเหลือ และใส่ทุกเอกสารที่อัปเดตในไดเรกทรอรี่ Martus\Docs\r\n \r\nคลิ๊กปุ่ม เสร็จสิ้น เพื่อออกจากโปรแกรม."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "คุณต้องการสร้างทางลัดไว้ที่ Start Menu ของคุณหรือไม่?"
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "คุณต้องสร้างทางลัดไว้ที่บนเดสทอปของคุณหรือไม่"
!insertmacro LANG_STRING LaunchProgramInfo_Text "ทางลัด Martus ได้สร้างไว้ที่โฟลเดอร์โปรแกรม $INSTDIR ใช้ทางลัดนี้ หรือทำสำเนาถึง เพื่อเปิด Martus"

!insertmacro LANG_STRING MartusShortcutDescription_Text "Martus ระบบรายงานด้านสิทธิมนุษยชน (ภาษาไทย)"

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "คู่มือใช้งาน (ภาษาไทย)"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide_th.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "คู่มือใช้งานฉบับย่อ (ภาษาไทย)"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide_th.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "ถอดถอนการติดตั้ง"

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "รูปแบบไฟล์รายงานของ Martus (ภาษาไทย)"

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "$(^Name) ได้ถูกลบออกจากเครื่องของคุณแล้ว"

!insertmacro LANG_STRING NeedAdminPrivileges_Text "คุณต้องได้รับสิทธิ์เป็นผู้ดูแลเครื่องนี้จึงจะสามารถติดตั้งโปรแกรม $(^Name)ได้"
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "เกิดความผิดพลาดในการตรวจสอบสิทธิ์ผู้ดูแลเครื่อง โปรดพิสูจน์ว่าคุณมีสิทธิ์เป็นผู้ดูแลเครื่องนี้ ถ้าไม่แล้วการติดตั้งโปรแกรม $(^Name) ไม่สามารถทำให้สำเร็จได้"

!insertmacro LANG_STRING UninstallProgramRunning_Text "โปรดตรวจสอบว่าคุณออกจากโปรแกรม $(^Name) แล้ว ถ้าไม่แล้วตัวถอดถอนจะไม่สามารถลบไฟล์ที่กำลังใช้อยู่ได้"

!insertmacro LANG_STRING NewerVersionInstalled_Text "รุ่น ($EXISTING_MARTUS_VERSION) ของ ${PRODUCT_NAME} เป็นรุ่นใหม่กว่าได้ติดตั้งไว้แล้ว  คุณต้องถอดถอนการติดตั้งรุ่นที่ติดติ้งไว้แล้วก่อนที่คุณจะสามารถติดตั้งรุ่นเก่านี้ได้ อย่างไรก็ตามถ้าคุณลดระดับรุ่น คุณจะเสียหน้าที่การทำงานบางอย่าง และไม่สามารถดูรายงานที่สร้างด้วยรุ่นใหม่กว่าได้  เพื่อรักษารุ่นใหม่ไว้ กดตกลงเพื่อออกจากการติดตั้งนี้  ถ้าคุณต้องการลดระดับรุ่นทั้งที่จะเสียหน้าที่การทำงานบางอย่าง ออกจากการติดตั้งนี้ ถอดถอนรุ่นใหม่ออกก่อน แล้วจึงติดตั้งรุ่นเก่าอีกครั้ง"
!insertmacro LANG_STRING SameVersionInstalled_Text "รุ่น ($EXISTING_MARTUS_VERSION) ของ ${PRODUCT_NAME} เป็นรุ่นที่ได้ติดตั้งอยู่แล้ว คุณต้องการติดตั้งอีกครั้งหรือไม่"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "รุ่น ($EXISTING_MARTUS_VERSION) ของ ${PRODUCT_NAME} เป็นรุ่นเก่ากว่า.  ตัวติดตั้งจะติดตั้งรุ่น ${PRODUCT_EXTENDED_VERSION} ให้"
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "การติดตั้งโปรแกรม ${PRODUCT_NAME} เมื่อสักครู่ปรากฏในเครื่องของคุณแล้ว เราจะพยายามเปิดตัวถอดถอนการติดตั้งและเมื่อถอดถอนเสร็จ ตัวติดตั้งปัจจุบันจะเริ่มดำเนินการ ถ้าคุณยังไม่ได้สำรองกุญแจของ Martus รุ่นปัจจุบัน เราแนะนำให้คุณออกจากการติดตั้งนี้ และทำการสำรองก่อนที่จะทำการถอดถอน  คุณสามารถเปิดตัวติดตั้งนี้อีกครั้งได้"
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "Martus รุ่นที่คุณทำการติดตั้ง สามารถอัพเกรดได้โดยใช้ตัวติดตั้งรุ่นเต็มซึ่งมี Java เท่านั้น"
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "คุณไม่สามารถลบ Martus รุ่นเก่าออกจากเครื่องของคุณได้ ตัวติดตั้งจะออกเดี๋ยวนี้ โปรดถอดถอน Martus โดยใช้ Add/Remove Programs ใน Control Panel และเปิดตัวติดตั้งนี้อีกครั้ง  ถ้าคุณยังไม่ได้ทำการสำรองกุญแจของ Martus รุ่นเก่า เราแนะนำให้คุณทำก่อนการถอดถอนการติดตั้ง"
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "นี่เป็นรุ่นปรับปรุงของโปรแกรม Martus กรุณาดาวน์โหลดและติดตั้งตัวติดตั้งรุ่นสมบูรณ์ที่มีจาวาอยู่ด้วย"

!insertmacro LANG_STRING UninstallMartusManuallyAndRemoveLinks_Text "You cannot upgrade from versions of Martus older than 4.3 to the current version.$\r$\n $\r$\nYou need to uninstall your earlier version of Martus (either under Start > Programs > Martus > Uninstall Martus, or using Add/Remove Programs in the Control Panel).$\r$\n $\r$\nIf you are running Windows Vista or Windows 7 and had a Martus shortcut in your Start menu, you may also need to remove the Martus group by going to Start > Programs, right-clicking on the Martus group, and picking 'Delete'.$\r$\nOtherwise those links will remain when you uninstall the current version of Martus, which could be a security issue."
