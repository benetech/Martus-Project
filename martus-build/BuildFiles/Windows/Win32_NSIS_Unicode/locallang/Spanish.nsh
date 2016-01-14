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

!define LANG "SPANISH" ; Required
;!insertmacro LANG_STRING <STRING_DEFINE> "string_value"

; language selection dialog stuff
!insertmacro LANG_STRING LangDialog_Title "Idioma de la instalación"
!insertmacro LANG_STRING LangDialog_Text "Por favor escoja el idioma del programa de instalación."

!insertmacro LANG_STRING FinishDialog_Text "${PRODUCT_NAME} ${PRODUCT_EXTENDED_VERSION} ha sido instalado en su sistema.\r\n \r\nVisite el sitio web https://www.martus.org/ para verificar si hay nuevos Paquetes de Idiomas (Language Packs) para ${PRODUCT_NAME} disponibles. \r\n \r\nUn (Paquete de idioma) le permite instalar traducciones o documentación nuevas y actualizadas en cualquier momento enseguida que esté disponible una nueva versión de Martus. Los 'Paquetes de idioma' pueden contener actualizaciones a las traducciones de la interfaz gráfica del cliente Martus, el Manual de Usuario, la Guía rápida/ Tarjeta de referencia, el archivo LÉAME, y ayuda dentro del programa.\r\n \r\nPresione Terminar para cerrar este asistente"
!insertmacro LANG_STRING FinishDialog2_Text "."

; shortcuts
!insertmacro LANG_STRING StartMenuShortcutQuestion_Text "¿Necesita enlaces a Martus en el menú Inicio?"
!insertmacro LANG_STRING DesktopShortcutQuestion_Text "¿Necesita un enlace a Martus sobre su escritorio?"
!insertmacro LANG_STRING LaunchProgramInfo_Text "La instalación va a colocar un enlace a Martus en la carpeta del programa en $INSTDIR. Use este enlace, o una copia de el, para iniciar Martus."

!insertmacro LANG_STRING MartusShortcutDescription_Text "Infraestructura de recogida de datos y gestión de la información de Martus"

!insertmacro LANG_STRING MartusUserGuideShortcut_Text "Manual de usuario"
!insertmacro LANG_STRING MartusUserGuideShortcut_Filename "martus_user_guide_es.pdf"

!insertmacro LANG_STRING MartusQuickstartShortcut_Text "Guía rápida"
!insertmacro LANG_STRING MartusQuickstartShortcut_Filename "quickstartguide_es.pdf"

!insertmacro LANG_STRING MartusUninstallShortcut_Text "Desinstalar"

; file property for .mba
!insertmacro LANG_STRING MartusMBAFileDesc_Text "Archivo de boletín de Martus"

; uninstall strings
!insertmacro LANG_STRING UninstallSuccess_Text "Se desinstaló $(^Name) de su ordenador."

!insertmacro LANG_STRING NeedAdminPrivileges_Text "Es necesario tener permiso de administración para poder instalar el software $(^Name) en su ordenador."
!insertmacro LANG_STRING NeedAdminPrivilegesError_Text "Ocurrió un error inesperado. Verifique que tenga permiso de administración para poder instalar el software $(^Name) en su ordenador. De no ser asi la instalación de $(^Name) podrá tener problemas."

!insertmacro LANG_STRING UninstallProgramRunning_Text "Por favor verifique de que no está usando $(^Name) o el programa de desinstalación no podrá remover aquellos archivos que esten en uso."

!insertmacro LANG_STRING NewerVersionInstalled_Text "Tiene una versión más reciente ($EXISTING_MARTUS_VERSION) de ${PRODUCT_NAME} instalado en su ordenador. Si quiere instalar la copia anterior va a tener que desinstalar la copia actual y volver a reiniciar la instalación de la copia anterior. Note que va a perder funcionalidad si decide continuar e instala una copia anterior y puede que no pueda ver o editar boletines creados con la versión más reciente. Para mantener la versión actual oprima Aceptar para salir del programa de instalación. Si es que decide instalar la versión anterior, oprima Aceptar para salir del programa de instalación, desinstale la versión actual y vuelva a intentar este programa de instalación."
!insertmacro LANG_STRING SameVersionInstalled_Text "La versión actual ($EXISTING_MARTUS_VERSION) de ${PRODUCT_NAME} ya está instalada. ¿Desea volver a instalarla?"
!insertmacro LANG_STRING UpgradeVersionInstalled_Text "Tiene una versión anterior ($EXISTING_MARTUS_VERSION) de ${PRODUCT_NAME} instalado en su ordenador.  El programa de instalación lo actualizará a la versión ${PRODUCT_EXTENDED_VERSION}."
!insertmacro LANG_STRING RemoveInstallShieldVersion_Text "Tiene una versión anterior de ${PRODUCT_NAME} en su ordenador. Intentaremos ejecutar el programa de desinstalación. Una vez este haya completado el programa de instalación de la versión actual continuará. Si es que aún no ha hecho una copia de seguridad de su par de claves le sugerimos salga del programa de instalación, haga una copia y vuelva a ejecutar este programa de instalación. ¿Desea continuar con la instalación?"
!insertmacro LANG_STRING CannotUpgradeNoJava_Text "La versión de Martus que tiene instalada no puede ser actualizada por este programa de instalación. Solo puede ser actualizada por el programa de instalación de Martus que contiene el entorno Java."
!insertmacro LANG_STRING CannotRemoveInstallShieldVersion_Text "No se pudo remover la versión anterior de Martus en su ordenador. El programa de instalación terminará, por favor remueva Martus de su ordenador usando la función para agregar y quitar programas en su panel de control y luego vuelva a ejecutar este programa de instalación. Antes de desinstalar Martus le sugerimos haga una copia de seguridad de su par de claves si es que aún no lo ha hecho."
!insertmacro LANG_STRING CannotUpgradeNoMartus_Text "Este programa de instalación es para actualizar una copia existente de Martus. Por favor descargue e instale el programa de instalación de Martus que contiene el entorno Java."

!insertmacro LANG_STRING UninstallMartusManuallyAndRemoveLinks_Text "No se puede actualizar desde versiones de Martus anteriores de 4.3 a la versión actual.$\r$\n $\r$\nTendrá que desinstalar la versión anterior de Martus (ya sea en Inicio > Programas > Martus > Desinstalar Martus, o mediante Agregar/Quitar Programas en el Panel de control). $\r$\n $\r$\nSi está ejecutando Windows Vista o Windows 7 y tenía un acceso directo a Martus en el menú de Inicio, también puede ser necesario eliminar el grupo de Martus dirigiéndose a Inicio > Todos los programas, hacer clic derecho en el grupo de Martus y escoger 'Eliminar'.$\r$\nSi no, esos vínculos se mantendrán al desinstalar la versión actual de Martus y podría ser un problema de seguridad."
