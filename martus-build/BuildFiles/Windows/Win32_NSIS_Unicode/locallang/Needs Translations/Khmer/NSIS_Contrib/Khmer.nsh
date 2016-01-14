;NSIS Modern User Interface - Language File
;Compatible with Modern UI 1.68

;Language: Khmer (1024) <- Neutral Lang Code
;Language: Khmer (1033) <- Neutral Lang Code
;Updated to NSIS 3 by Charles LaPierre
;--------------------------------
!insertmacro LANGFILE "Khmer" = "ភាសាខ្មែរ, Khmer" =

 !ifdef MUI_WELCOMEPAGE
   ${LangFileString}  MUI_TEXT_WELCOME_INFO_TITLE "ស្វាគមន៍​មក​កាន់​អ្នក​ជំនួយ​ការ​ដំឡើង $(^NameDA)"
   ${LangFileString}  MUI_TEXT_WELCOME_INFO_TEXT "​អ្នក​ជំនួយ​ការ​​នេះ​នឹង​ណែនាំ​អ្នក​ក្នុង​ការដំឡើង នឹង​ណែនាំ​អ្នក​ក្នុង​ការ​បញ្ចូល$(^NameDA) ។$\r$\n$\r$\nយើង​សុំ​ស្នើរ​អោយ​អ្នក​បិទ​រាល់​កម្មវិធី​ផ្សេងៗ​មុន​នឹង​ចាប់ផ្តើម​ការដំឡើង ។​ ធ្វើ​ដូច្នេះ​នឹងផ្តល់​លទ្ធភាព​ក្នុង​ការ​អភិវឌ្ឍ​​ឯកសារ​នៃ​ប្រព័ន្ធ​ដែល​ទាក់​ទង​ដោយ​ពុំ​ចាំបាច់​ចាប់​ផ្តើម​កុំព្យូទ័រ​របស់​អ្នក​ឡើង​វិញ ។$\r$\n$\r$\n$_CLICK"
!endif

!ifdef MUI_UNWELCOMEPAGE
  ${LangFileString} MUI_UNTEXT_WELCOME_INFO_TITLE "ស្វាគមន៍​មក​កាន់​អ្នក​ជំនួយ​ការ​លុប $(^NameDA)"
  ${LangFileString} MUI_UNTEXT_WELCOME_INFO_TEXT "អ្នក​ជំនួយ​ការ​​នេះ​នឹង​ណែនាំ​អ្នក​ក្នុង​ការ​លុប $(^NameDA) ។$\r$\n$\r$\nមុន​ពេល​ចាប់​ផ្តើម​ដំណើរ​ការ​លុប សូម​ពិនិត្យ​មើល​ថា $(^NameDA) មិន​កំពុង​ដំ​ណើរ​ការ​ទេ ។$\r$\n$\r$\n$_CLICK"
!endif

!ifdef MUI_LICENSEPAGE
  ${LangFileString} MUI_TEXT_LICENSE_TITLE "កិច្ច​ព្រមព្រៀង​អាជ្ញាប័ណ្ណ"  
  ${LangFileString} MUI_TEXT_LICENSE_SUBTITLE "សូម​ពិនិត្យ​មើល​លក្ខខណ្ឌ​នៃកិច្ច​ព្រមព្រៀង អាជ្ញាប័ណ្ណ​មុន​ពេលដំឡើង $(^NameDA) ។"
  ${LangFileString} MUI_INNERTEXT_LICENSE_BOTTOM "បើ​អ្នក​ព្រម​ទទួលយក​តាម​លក្ខខណ្ឌ​នៃ​កិច្ច​ព្រមព្រៀង សូម​ចុច 'ខ្ញុំយល់ព្រម' ដើម្បី​បន្ត ។ អ្នក​ត្រូវ​តែ​ព្រម​ទទួល​យក​កិច្ច​ព្រមព្រៀង​ដើម្បី​ដំឡើង $(^NameDA) ។"
  ${LangFileString} MUI_INNERTEXT_LICENSE_BOTTOM_CHECKBOX "បើ​អ្នក​ព្រម​ទទួលយក​តាម​លក្ខខណ្ឌ​នៃ​កិច្ច​ព្រមព្រៀង សូម​គូសធីក​ប្រអប់​ខាង​ក្រោម ។ អ្នក​ត្រូវ​តែ​ព្រម​ទទួល​យក​កិច្ច​ព្រមព្រៀង​ដើម្បី​ដំឡើង $(^NameDA) $_CLICK"
  ${LangFileString} MUI_INNERTEXT_LICENSE_BOTTOM_RADIOBUTTONS "បើ​អ្នក​ព្រម​ទទួលយក​តាម​លក្ខខណ្ឌ​នៃ​កិច្ច​ព្រមព្រៀង សូម​ជ្រើសជម្រើស​ទី​មួយ​ខាង​ក្រោម ។ អ្នក​ត្រូវ​តែ​ព្រម​ទទួល​យក​កិច្ច​ព្រមព្រៀង​ដើម្បី​ដំឡើង $(^NameDA) ។ $_CLICK"
!endif

!ifdef MUI_UNLICENSEPAGE 
  ${LangFileString} MUI_UNTEXT_LICENSE_TITLE "កិច្ច​ព្រមព្រៀង​អាជ្ញាប័ណ្ណ"  
  ${LangFileString} MUI_UNTEXT_LICENSE_SUBTITLE "សូម​ពិនិត្យ​មើល​លក្ខខណ្ឌ​នៃ​អាជ្ញាប័ណ្ណមុន​ពេល​លុប $(^NameDA) ។"
  ${LangFileString} MUI_UNINNERTEXT_LICENSE_BOTTOM "បើ​អ្នក​ព្រម​ទទួលយក​តាម​លក្ខខណ្ឌ​នៃ​កិច្ច​ព្រមព្រៀង សូម​ចុច 'ខ្ញុំយល់ព្រម' ដើម្បី​បន្ត ។ អ្នក​ត្រូវ​តែ​ព្រម​ទទួល​យក​កិច្ច​ព្រមព្រៀង ដើម្បី​លុប $(^NameDA) ។"
  ${LangFileString} MUI_UNINNERTEXT_LICENSE_BOTTOM_CHECKBOX "បើ​អ្នក​ព្រម​ទទួលយក​តាម​លក្ខខណ្ឌ​នៃ​កិច្ច​ព្រមព្រៀង សូម​គូស​ធីកប្រអប់​ខាង​ក្រោម ។ អ្នក​ត្រូវ​តែ​ព្រម​ទទួល​យក​កិច្ច​ព្រមព្រៀង ដើម្បី​លុប $(^NameDA) ។ $_CLICK"
  ${LangFileString} MUI_UNINNERTEXT_LICENSE_BOTTOM_RADIOBUTTONS "បើ​អ្នក​ព្រម​ទទួលយក​តាម​លក្ខខណ្ឌ​នៃ​កិច្ច​ព្រមព្រៀង សូម​ជ្រើស​ជម្រើស​ទី​មួយ​ខាង​ក្រោម ។ អ្នក​ត្រូវ​តែ​ព្រម​ទទួល​យក​កិច្ច​ព្រមព្រៀង ដើម្បី​លុប $(^NameDA) ។ $_CLICK"
!endif

!ifdef MUI_LICENSEPAGE | MUI_UNLICENSEPAGE
  ${LangFileString} MUI_INNERTEXT_LICENSE_TOP "សូម​ចុច "ទំព័រក្រោម" ដើម្បី​មើល​ដល់​ចុង​បញ្ចប់​នៃ​កិច្ច​ព្រមព្រៀង ។"
!endif
  
!ifdef MUI_COMPONENTSPAGE
  ${LangFileString} MUI_TEXT_COMPONENTS_TITLE "ជ្រើស​សមាសភាគ"
  ${LangFileString} MUI_TEXT_COMPONENTS_SUBTITLE "ជ្រើស​លក្ខណៈ​ពិសេស​ទាំង​ឡាយ​ណា​របស់ $(^NameDA) ដែល​អ្នក​ចង់​ដំឡើង ។"
!endif

!ifdef MUI_UNCOMPONENTSPAGE
  ${LangFileString} MUI_UNTEXT_COMPONENTS_TITLE "ជ្រើស​សមាសភាគ"
  ${LangFileString} MUI_UNTEXT_COMPONENTS_SUBTITLE "ជ្រើស​លក្ខណៈ​ពិសេស​ទាំង​ឡាយ​ណា​របស់ $(^NameDA) ដែល​អ្នក​ចង់​លុប។"
!endif  


!ifdef MUI_COMPONENTSPAGE | MUI_UNCOMPONENTSPAGE
  ${LangFileString} MUI_INNERTEXT_COMPONENTS_DESCRIPTION_TITLE "ការ​ពិពណ៌នា"
  !ifndef NSIS_CONFIG_COMPONENTPAGE_ALTERNATIVE
	${LangFileString} MUI_INNERTEXT_COMPONENTS_DESCRIPTION_INFO "ដាក់​កណ្តុរ​របស់​អ្នក​លើ​សមាសភាគ​ដើម្បី​មើល​ការពិពណ៌នា​អំពី​វា ។"
 !else
	${LangFileString} MUI_INNERTEXT_COMPONENTS_DESCRIPTION_INFO "ដាក់​កណ្តុរ​របស់​អ្នក​លើ​សមាសភាគ​ដើម្បី​មើល​ការពិពណ៌នា​អំពី​វា ។"
  !endif
!endif

!ifdef MUI_DIRECTORYPAGE
  ${LangFileString} MUI_TEXT_DIRECTORY_TITLE "ជ្រើស​រើស​ទី​តាំងសម្រាប់ដំឡើង"
  ${LangFileString} MUI_TEXT_DIRECTORY_SUBTITLE "ជ្រើសប្រអប់សម្រាប់ដំឡើង $(^NameDA) ។"
!endif
  
!ifdef MUI_UNDIRECTORYPAGE
  ${LangFileString} MUI_UNTEXT_DIRECTORY_TITLE "ជ្រើស​ទី​តាំង​ដែល​ត្រូវលុប"
  ${LangFileString} MUI_UNTEXT_DIRECTORY_SUBTITLE "ជ្រើសថតដែល​ត្រូវ​លុប $(^NameDA) ។"
!endif
  

!ifdef MUI_INSTFILESPAGE
  ${LangFileString} MUI_TEXT_INSTALLING_TITLE "កំពុងដំឡើង"
  ${LangFileString} MUI_TEXT_INSTALLING_SUBTITLE "សូម​រង់​ចាំ​ខណៈ​ពេល​ដែល $(^NameDA) កំពុង​ត្រូវ​បាន​ដំឡើង ។"  
  ${LangFileString} MUI_TEXT_FINISH_TITLE "ការដំឡើង​ចប់​ហើយ"
  ${LangFileString} MUI_TEXT_FINISH_SUBTITLE "ការ​ដំឡើង​ត្រូវ​បាន​បញ្ចប់​ដោយ​ជោគជ័យ ។"
  ${LangFileString} MUI_TEXT_ABORT_TITLE "ការ​ដំឡើង​ត្រូវ​​បាន​បញ្ឈប់"
  ${LangFileString} MUI_TEXT_ABORT_SUBTITLE "ការដំឡើង​មិន​បាន​បញ្ចប់​ដោយ​ជោគជ័យ​ទេ ។"
!endif

!ifdef MUI_UNINSTFILESPAGE
  ${LangFileString} MUI_UNTEXT_UNINSTALLING_TITLE "កំពុង​លុប"
  ${LangFileString} MUI_UNTEXT_UNINSTALLING_SUBTITLE "សូម​រង់​ចាំ ខណៈ​ពេល​ដែល $(^NameDA) កំពុង​ត្រូវ​បាន​លុប ។"
  ${LangFileString} MUI_UNTEXT_FINISH_TITLE "ការ​លុប​បញ្ចប់​ទាំង​ស្រុង"
  ${LangFileString} MUI_UNTEXT_FINISH_SUBTITLE "ការ​លុប​ត្រូវ​បាន​បញ្ចប់​ដោយ​ជោគជ័យ ។"
  ${LangFileString} MUI_UNTEXT_ABORT_TITLE "ការ​លុប​ត្រូវ​បញ្ឈប់"
  ${LangFileString} MUI_UNTEXT_ABORT_SUBTITLE "ការ​លុប​មិន​បាន​បញ្ចប់​ដោយ​ជោគជ័យ​ទេ ។"
!endif

!ifdef MUI_FINISHPAGE  
  ${LangFileString} MUI_TEXT_FINISH_INFO_TITLE "បញ្ចប់​អ្នក​ជំនួយ​ការ​ដំឡើង $(^NameDA)"
  ${LangFileString} MUI_TEXT_FINISH_INFO_TEXT "$(^NameDA) ត្រូវ​បាន​ដំឡើង​ទៅ​ក្នុង​កុំព្យូទ័រ​របស់​អ្នក​ហើយ ។​$\r$\n$\r$\nសូម​ចុច 'បញ្ចប់' ដើម្បី​បិទ​អ្នក​ជំនួយ​ការនេះ ។"
  ${LangFileString} MUI_TEXT_FINISH_INFO_REBOOT "អ្នក​ត្រូវ​តែ​ចាប់​ផ្តើម​កុំព្យូទ័រ​របស់​អ្នក​ឡើង​វិញ​ដើម្បី​បញ្ចប់​ការ​ដំឡើង $(^NameDA) ទាំង​ស្រុង ។ តើ​អ្នក​ចង់​ចាប់​ផ្តើម​ឡើង​វិញ​ឥឡូវ​នេះ​ឬ ?"
!endif

!ifdef MUI_UNFINISHPAGE
  ${LangFileString} MUI_UNTEXT_FINISH_INFO_TITLE "បញ្ចប់ អ្នក​ជំនួយ​ការ​ការ​លុប $(^NameDA)"
  ${LangFileString} MUI_UNTEXT_FINISH_INFO_TEXT "$(^NameDA) ត្រូវ​បាន​លុប​ចេញ​ពី​កុំព្យូទ័រ​របស់​អ្នក​ហើយ ។​$\r$\n$\r$\nសូម​ចុច 'បញ្ចប់' ដើម្បី​បិទ​អ្នក​ជំនួយ​ការនេះ ។"
  ${LangFileString} MUI_UNTEXT_FINISH_INFO_REBOOT "អ្នក​ត្រូវ​តែ​ចាប់​ផ្តើម​កុំព្យូទ័រ​របស់​អ្នក​ឡើង​វិញ ដើម្បី​បញ្ចប់​ការ​លុប $(^NameDA) ចេញទាំង​ស្រុង ។ តើ​អ្នក​ចង់​ចាប់​ផ្តើម​ឡើង​វិញ​ឥឡូវ​នេះ​ឬ ?"
!endif

!ifdef MUI_FINISHPAGE | MUI_UNFINISHPAGE
  ${LangFileString} MUI_TEXT_FINISH_REBOOTNOW "ចាប់​ផ្តើម​ឡើង​វិញ​ឥឡូវ​នេះ"
  ${LangFileString} MUI_TEXT_FINISH_REBOOTLATER "ខ្ញុំ​ចង់​ចាប់​ផ្តើម​ឡើង​វិញ​ដោយ​ដៃ​នៅ​ពេល​ក្រោយ"
  ${LangFileString} MUI_TEXT_FINISH_RUN "&ដំណើរ​ការ $(^NameDA)"
  ${LangFileString} MUI_TEXT_FINISH_SHOWREADME "បង្ហាញ​&ឯកសារអាន​ខ្ញុំ"
  ${LangFileString} MUI_BUTTONTEXT_FINISH "ប&ញ្ចប់"
!endif

!ifdef MUI_STARTMENUPAGE
  ${LangFileString} MUI_TEXT_STARTMENU_TITLE "ជ្រើស​ថតម៉ឺនុយ​ចាប់ផ្តើម"
  ${LangFileString} MUI_TEXT_STARTMENU_SUBTITLE "ជ្រើស​ថតម៉ឺនុយ​ចាប់ផ្តើម សម្រាប់​ផ្លូវ​កាត់​របស់ $(^NameDA) ។"
  ${LangFileString} MUI_INNERTEXT_STARTMENU_TOP "សូម​ជ្រើស​ថតម៉ឺនុយ​ចាប់ផ្តើម ដែល​អ្នក​ចង់​បង្កើត​ផ្លូវ​កាត់របស់​កម្មវិធី​ក្នុង​នោះ។ អ្នក​ក៏​អាច​បញ្ចូល​ឈ្មោះ​ដើម្បី​បង្កើតថត​ថ្មី​ផង​ដែរ ។"
  ${LangFileString} MUI_INNERTEXT_STARTMENU_CHECKBOX "កុំ​បង្កើត​ផ្លូវ​កាត់"
!endif

!ifdef MUI_UNCONFIRMPAGE
  ${LangFileString} MUI_UNTEXT_CONFIRM_TITLE "លុប $(^NameDA)"
  ${LangFileString} MUI_UNTEXT_CONFIRM_SUBTITLE "ដក $(^NameDA) ចេញ​ពី​កុំព្យូទ័រ​របស់​អ្នក ។"
!endif

!ifdef MUI_ABORTWARNING  
  ${LangFileString} MUI_TEXT_ABORTWARNING "តើ​អ្នក​ពិត​ជា​ចង់​បោះបង់​ការ​ដំឡើង $(^Name) មែន​ទេ ?"
!endif

!ifdef MUI_UNABORTWARNING  
  ${LangFileString} MUI_UNTEXT_ABORTWARNING "តើ​អ្នក​ពិត​ជា​ចង់​បោះបង់​ការ​លុប $(^Name) មែន​ទេ ?"
!endif
  
!ifdef MULTIUSER_INSTALLMODEPAGE
  ${LangFileString} MULTIUSER_TEXT_INSTALLMODE_TITLE "Choose Users"
  ${LangFileString} MULTIUSER_TEXT_INSTALLMODE_SUBTITLE "Choose for which users you want to install $(^NameDA)."
  ${LangFileString} MULTIUSER_INNERTEXT_INSTALLMODE_TOP "Select whether you want to install $(^NameDA) for yourself only or for all users of this computer. $(^ClickNext)"
  ${LangFileString} MULTIUSER_INNERTEXT_INSTALLMODE_ALLUSERS "Install for anyone using this computer"
  ${LangFileString} MULTIUSER_INNERTEXT_INSTALLMODE_CURRENTUSER "Install just for me"
!endif
