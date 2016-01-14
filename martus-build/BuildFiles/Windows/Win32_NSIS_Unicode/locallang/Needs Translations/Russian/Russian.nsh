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

!define LANG "RUSSIAN" ; Required  
;!insertmacro LANG_STRING <STRING_DEFINE> "string_value"
!insertmacro LANG_STRING LangDialog_Title "Язык интерфейса"
!insertmacro LANG_STRING LangDialog_Text "Выберите язык интерфейса программы установки."

!insertmacro LANG_STRING FinishDialog_Text "Установка ${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} выполнена. Загрузите обновленные локализованные пакеты документации Martus с сайта: https://www.martus.org/downloads. Локализованный пакет документации  позволяет устанавливать исправленные переводы документации после выпуска новой версии Martus. В локализованный пакет могут входить: исправленные версии локализованного клиентского интерфейса Martus, Руководство пользователя, Экспресс-справка, файл README и программу справка. Если доступна ссылка на русскоязычный пакет для Вашей версии Martus, загрузите файл Martus-ru.mlp и поместите его в Вашем каталоге Martus. При следующем запуске Martus на Вашем компьютере загрузятся все исправленные переводы и подсказки, а новые редакции документации будут помещены в каталог Martus\Docs. Нажмите кнопку 'Готово' для выхода из программы установки."
 
; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "Добавить ярлык Martus в главное меню Windows (меню 'Пуск')? "
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "Разместить ярлык  Martus на Рабочем столе?"
!insertmacro LANG_STRING LaunchProgramInfo_Text "Ярлык Martus установлен в папке программ  $INSTDIR. Чтобы запустить Martus, используйте этот ярлык или его копию."
 
!insertmacro LANG_STRING MartusShortcutDescription_Text "Martus - система для создания сводок по правам человека"
 
!insertmacro LANG_STRING MartusUserGuideShortcut_Text "Руководство пользователя"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide_ru.pdf"
 
!insertmacro LANG_STRING MartusQuickstartShortcut_Text "Краткая справка"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide_ru.pdf"
 
!insertmacro LANG_STRING MartusUninstallShortcut_Text "Удалить программу"
 
; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "Архив сводок Martus"
 
; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "Программа $(^Name) была успешно удалена с Вашего компьютера"
 
!insertmacro LANG_STRING NeedAdminPrivileges_Text "Для установки программы $(^Name) Вам потребуются права администратора на данном компьютере."
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "Неустановленная ошибка при получении прав администратора. Проверьте, имеете ли Вы права администратора для данного компьютера. Если это не так, установить  $(^Name) будет невозможно."
 
!insertmacro LANG_STRING UninstallProgramRunning_Text "Проверьте, что Вы вышли из  $(^Name). В противном случае программа удаления не сможет удалить файлы, которые используются в данный момент."
 
!insertmacro LANG_STRING NewerVersionInstalled_Text "На компьютере уже установлена более новая версия ($EXISTING_MARTUS_VERSION) программы  ${PRODUCT_NAME}.  Прежде чем заново устанавливать более раннюю версию, удалите имеющийся экземпляр программы. Предупреждаем, что при переходе к более ранней версии часть функциональных возможностей будет потеряна.  Чтобы оставить на компьютере более новую версию программы, нажмите OK для выхода из программы установки.  Если Вы желаете установить более раннюю версию с более ограниченными функциональными возможностями, выйдите из программы установки, удалите более новую версию, а затем установите данную версию."
!insertmacro LANG_STRING SameVersionInstalled_Text "Текущая версия ($EXISTING_MARTUS_VERSION) программного продукта ${PRODUCT_NAME} уже установлена. Установить заново?"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "Установлена более ранняя версия ($EXISTING_MARTUS_VERSION) программного продукта ${PRODUCT_NAME}. Программа установки обновит ее до версии ${PRODUCT_EXTENDED_VERSION}."
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "На Вашем компьютере уже установлено программное обеспечение ${PRODUCT_NAME}. Будет сделана попытка запустить программу для ее удаления. По завершении удаления текущий процесс установки будет продолжен. Если Вы не сохраняли резервную копию ключей Вашей текущей версии Martus, советуем Вам прервать текущий процесс установки и сохранить резервную копию ключей, а затем  удалить имеющийся экземпляр программы. После этого можо будет заново запустить  данную программу установки."
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "Обновить версию Martus, установленную на данном компьютере, можно только с помощью полной версии программы установки, которая включает в себя Java."
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "Не удалось удалить с данного компьютера прежнюю версию  Martus. После того, как программа установки завершит свою работу,  удалите имеющийся экземпляр программного обеспечения  Martus с помощью команды 'Установка и удаление программ' Панели управления и запустите данную программу установки заново. Если Вы не сохранили резервную копию ключей Вашей текущей версии Martus, советуем Вам сделать это, прежде чем удалять программу."
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "Данный загрузочный модуль предназначен для обновления версии Martus. Загрузите и установите полный загрузочный модуль данной версии, содержащий Java."

!insertmacro LANG_STRING UninstallMartusManuallyAndRemoveLinks_Text "You cannot upgrade from versions of Martus older than 4.3 to the current version.$\r$\n $\r$\nYou need to uninstall your earlier version of Martus (either under Start > Programs > Martus > Uninstall Martus, or using Add/Remove Programs in the Control Panel).$\r$\n $\r$\nIf you are running Windows Vista or Windows 7 and had a Martus shortcut in your Start menu, you may also need to remove the Martus group by going to Start > Programs, right-clicking on the Martus group, and picking 'Delete'.$\r$\nOtherwise those links will remain when you uninstall the current version of Martus, which could be a security issue."
 
