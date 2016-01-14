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

!define LANG "SIMPCHINESE" ; Required
;!insertmacro LANG_STRING <STRING_DEFINE> "string_value"

; language selection dialog stuff
!insertmacro LANG_STRING LangDialog_Title "安装语言"
!insertmacro LANG_STRING LangDialog_Text "请选择安装语言。"

!insertmacro LANG_STRING FinishDialog_Text "${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} 已安装在您的电脑上。\r\n \r\n 请访问 https://www.martus.org/，查看是否有任何更新的 Martus语言包可供选择。 \r\n \r\n语言包让您可在完整的 Martus 版本发布之后随时安装新的和更新的翻译或文档。语言包可含 Martus 客户端用户界面翻译、用户指南、快速入门指南、自述文件和程序内帮助的更新。\r\n \r\n单击“Finish”（完成）关闭此向导"
!insertmacro LANG_STRING FinishDialog2_Text "."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "您想在 Windows 开始菜单内安装 Martus 快捷方式吗？"
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "您想在桌面上安装 Martus 快捷方式吗？"
!insertmacro LANG_STRING LaunchProgramInfo_Text "已在程序文件夹 $INSTDIR 内安装 Martus 快捷方式。使用此快捷方式或其副本启动 Martus。"

!insertmacro LANG_STRING MartusShortcutDescription_Text "Martus 人权公告系统 （Martus Human Rights Bulletin System）"

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "用户指南"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "快速入门"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "卸载"

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "Martus 公告存档"

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "$(^Name) 已成功从您的电脑中删除。"

!insertmacro LANG_STRING NeedAdminPrivileges_Text "您需有本地机器的管理权限才能安装 $(^Name)。"
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "获取管理权限时发生未知错误。请确认您在这台机器上有管理权限，否则 $(^Name) 安装可能不会成功。"

!insertmacro LANG_STRING UninstallProgramRunning_Text "请确认您已退出 $(^Name)，否则卸载程序将无法删除正在使用的文件。"

!insertmacro LANG_STRING NewerVersionInstalled_Text "已安装了 ${PRODUCT_NAME} 的较新版本 ($EXISTING_MARTUS_VERSION)。您必须先卸载现有版本，然后才能安装此旧版本。但是，降级后将失去某些功能，而且可能无法查看使用较新的版本创建的公告。要保留较新的版本，按“OK”（确定）退出此安装。如果您在可能失去功能的情况下仍要降级，请退出此安装，先卸载较新的版本，然后重新安装这个旧版本。"
!insertmacro LANG_STRING SameVersionInstalled_Text "已安装了 ${PRODUCT_NAME} 的当前版本 ($EXISTING_MARTUS_VERSION)。您是否想重新安装？"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "已安装了 ${PRODUCT_NAME} 的旧版本 ($EXISTING_MARTUS_VERSION)。安装程序会将其升级到版本 ${PRODUCT_EXTENDED_VERSION}。"
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "您的电脑上已有 ${PRODUCT_NAME} 的旧版本。我们将尝试启动其卸载程序，当前的安装将在卸载完成后继续进行。如果您尚未对当前的 Martus 版本进行密钥备份，建议您退出此安装，在卸载前先备份，然后再运行此安装程序。您是否想继续安装？"
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "您已安装的 Martus 版本只能用带 Java 的完整版安装程序进行升级。"
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "我们无法从您的电脑删除 Martus 的旧版本。安装程序现将退出。请在“Control Panel”（控制面板）内使用“Add/Remove Programs”（添加/删除程序）删除 Martus，然后重新运行安装程序。如果您尚未对当前的 Martus 版本进行密钥备份，建议您在卸载前先备份。"
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "这是 Martus 的升级版本。请下载并安装带 Java 的完整版安装程序。"

!insertmacro LANG_STRING UninstallMartusManuallyAndRemoveLinks_Text "无法从 Martus 4.3 之前的版本升级到当前版本。$\r$\n $\r$\n需要先卸载 Martus 的早期版本（可用“Start > Programs > Martus > Uninstall Martus”（开始 > 程序 > Martus > 卸载 Martus），或用 “Control Panel”（控制面板）内的“Add/Remove Programs”（添加/删除程序）。$\r$\n $\r$\n若操作系统是 Windows Vista 或 Windows 7，且开始菜单中有 Martus 快捷方式，可能还需要删除 Martus 组。删除时可单击“Start > Programs”（开始 > 程序），右键单击 Martus 组，然后选择“Delete”（删除）。$\r$\n $\r$\n否则，卸载 Martus 当前版本时，这些链接将保留，可能带来安全问题。"
