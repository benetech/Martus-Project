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

!define LANG "FRENCH" ; Required
;!insertmacro LANG_STRING <STRING_DEFINE> "valeur de la chaîne"
!insertmacro LANG_STRING LangDialog_Title "Langue d'Installation. "
!insertmacro LANG_STRING LangDialog_Text "Veuillez sélectionner la langue d'installation."

!insertmacro LANG_STRING FinishDialog_Text "${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} a été installé sur votre ordinateur.\r\n \r\n Veuillez vous rendre sur https://www.martus.org/downloads pour télécharger les packs de langues Martus mis à jour. \r\n \r\nUn (Pack Langues) vous permet d'installer à tout moment les nouvelles mises à jour de traductions ou de documentation à la suite d'une nouvelle publication de Martus. Les Packs Langues peuvent contenir les mises à jour de la traduction de l'Interface Utilisateur Client Martus, du Guide de l'Utilisateur, du Guide de Démarrage Rapide, du fichier README (LISEZMOI) et de l'aide incluse dans le programme.\r\n \r\nCliquez sur Fermer pour quitter le programme d'installation"
!insertmacro LANG_STRING FinishDialog2_Text "."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "Voulez-vous installer un raccourci Martus dans votre menu démarrer Windows ?"
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "Voulez-vous installer un raccourci Martus sur votre bureau ?"
!insertmacro LANG_STRING LaunchProgramInfo_Text "Un raccourci Martus a été installé dans le dossier programme $INSTDIR. Utilisez ce raccourci, ou une copie, pour ouvrir Martus. "

!insertmacro LANG_STRING MartusShortcutDescription_Text "Système de Communiqués Martus pour les Droits Humains "

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "Guide de l'Utilisateur "
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide_fr.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "Démarrage Rapide "
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide_fr.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "Désinstaller "

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "Archives de Communiqués Martus "

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "$(^Name) a été supprimé de votre ordinateur. "

!insertmacro LANG_STRING NeedAdminPrivileges_Text "Il vous faut un privilège administratif sur cet ordinateur pour pouvoir installer $(^Name) "
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "Erreur inconnue pendant la recherche de privilèges administratifs. Assurez-vous d'avoir les privilèges administratifs sur cet ordinateur, sinon l'installation de $(^Name) risque d'échouer "

!insertmacro LANG_STRING UninstallProgramRunning_Text "Veuillez vous assurer d'avoir quitté $(^Name) sinon le programme de désinstallation ne pourra pas supprimer les fichiers utilisés. "

!insertmacro LANG_STRING NewerVersionInstalled_Text "Une version plus récente  ($EXISTING_MARTUS_VERSION) de ${PRODUCT_NAME} est déjà installée. Il vous faut d'abord désinstaller la version existante avant de pouvoir installer cette ancienne version. Si vous confirmez, vous perdrez cependant certaines fonctionnalités et risquez de ne pas pouvoir lire les communiqués créés avec la version récente. Pour conserver la version récente, tapez sur OK pour quitter cette installation. Si vous souhaitez quand même passer à l'ancienne version malgré la perte de fonctionnalité, quittez cette installation, désinstallez la version récente, puis réinstallez l'ancienne version. "
!insertmacro LANG_STRING SameVersionInstalled_Text "La version actuelle ($EXISTING_MARTUS_VERSION) de ${PRODUCT_NAME} est déjà installée. Voulez-vous la réinstaller ? "
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "Une version plus ancienne ($EXISTING_MARTUS_VERSION) de ${PRODUCT_NAME} est installée. Le programme d'installation va la remplacer par la version ${PRODUCT_EXTENDED_VERSION}. "
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "Il existe une installation antérieure de ${PRODUCT_NAME} sur votre ordinateur. Nous allons tenter de lancer le programme d'installation pour celle-ci, et ensuite l'installation actuelle reprendra. Si vous n'avez pas effectué une sauvegarde de clé dans votre version actuelle de Martus, nous vous conseillons de quitter cette installation et d'effectuer une sauvegarde avant de désinstaller. Ensuite, vous pourrez relancer ce programme d'installation. "
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "La version de Martus que vous avez installée ne peut être mise à jour qu'avec le programme d'installation complet qui contient Java. "
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "Nous n'avons pas pu supprimer de votre ordinateur l'ancienne version de Martus. Le programme d'installation va maintenant se fermer, veuillez supprimer votre copie de Martus à l'aide de Ajout/Suppression de Programmes dans le Panneau de Configuration, puis relancez ce programme d'installation. Si vous n'avez pas effectué une sauvegarde de clé dans votre version actuelle de Martus, nous vous conseillons de le faire avant la désinstallation. "
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "C'est une version de mise à niveau de Martus. Téléchargez et veuillez installez le plein installateur de version qui porte Java."

!insertmacro LANG_STRING UninstallMartusManuallyAndRemoveLinks_Text "You cannot upgrade from versions of Martus older than 4.3 to the current version.$\r$\n $\r$\nYou need to uninstall your earlier version of Martus (either under Start > Programs > Martus > Uninstall Martus, or using Add/Remove Programs in the Control Panel).$\r$\n $\r$\nIf you are running Windows Vista or Windows 7 and had a Martus shortcut in your Start menu, you may also need to remove the Martus group by going to Start > Programs, right-clicking on the Martus group, and picking 'Delete'.$\r$\nOtherwise those links will remain when you uninstall the current version of Martus, which could be a security issue."
