;NSIS Modern User Interface - Language File
;Compatible with Modern UI 1.68
;Language: Burmese (1117) <- arbitrary language number that hopefully doesn't conflict with others
;Updated to NSIS 3 by Kevin Smith
;File needs to be saved in UTF8 with BOM
;This commented language # above doesn't seem to matter at all by Charles LaPierre
!insertmacro LANGFILE "Burmese" = "ျမန္မာ, Burmese" =

!ifdef MUI_WELCOMEPAGE
  ${LangFileString} MUI_TEXT_WELCOME_INFO_TITLE "$(^NameDA) စတင္တပ္ဆင္မႈမွ ႀကိဳဆုိပါသည္"
  ${LangFileString} MUI_TEXT_WELCOME_INFO_TEXT "စတင္တပ္ဆင္မႈက သင့္ကုိ  $(^NameDA).$\r$\n$\r$\nအား တပ္ဆင္မႈကုိ လမ္းၫႊန္ေပးပါမည္။ စတင္တပ္ဆင္မႈကုိ မစခင္မွာ အျခားေသာ ပရုိဂရမ္မ်ား အားလံုးကုိ ပိတ္ပစ္ပါရန္ မိမိတုိ႔ အၾကံေပးလုိပါသည္။ ထုိသုိ႔ဆုိလွ်င္ သင္၏ ကြန္ပ်ဴတာကုိ ျပန္ဖြင့္ရန္ မလုိဘဲ ဆက္စပ္ စနစ္ဆုိင္ရာ ဖုိင္မ်ားကုိ အာပ္ဒိတ္ ျပဳလုပ္၍ ရႏုိင္ပါမည္။$\r$\n$\r$\n$_CLICK"  
!endif

!ifdef MUI_UNWELCOMEPAGE
  ${LangFileString} MUI_UNTEXT_WELCOME_INFO_TITLE "$(^NameDA) အား ျပန္ျဖဳတ္မႈမွ ႀကိဳဆုိပါသည္"
  ${LangFileString} MUI_UNTEXT_WELCOME_INFO_TEXT "စတင္တပ္ဆင္မႈက သင့္ကုိ $(^NameDA).$\r$\n$\r$\nအား ျပန္ျဖဳတ္မႈကုိ လမ္းၫႊန္ေပးပါမည္။ ျပန္ျဖဳတ္မႈကုိ မစတင္ခင္မွာ၊ $(^NameDA) ကုိ ဖြင့္မထားေၾကာင္း ေသခ်ာပါေစ။$\r$\n$\r$\n$_CLICK"
!endif

!ifdef MUI_LICENSEPAGE
  ${LangFileString} MUI_TEXT_LICENSE_TITLE "လုိင္စင္ သေဘာတူညီခ်က္"
  ${LangFileString} MUI_TEXT_LICENSE_SUBTITLE "ေက်းဇူးျပဳၿပီး $(^NameDA) ကုိ မတပ္ဆင္မီမွာ လုိင္စင္ စည္းကမ္းခ်က္မ်ားကုိ ဆန္းစစ္ၾကည့္ပါ။"
  ${LangFileString} MUI_INNERTEXT_LICENSE_BOTTOM "အကယ္၍ သင္က သေဘာတူညီခ်က္၏ စည္းကမ္းခ်က္မ်ားကုိ လက္ခံလွ်င္၊ ဆက္လုပ္ရန္ ကြၽႏ္ုပ္ သေဘာတူ ကုိ ႏွိပ္ပါ။ သင္သည္ $(^NameDA) ကုိ တပ္ဆင္ရန္အတြက္ သေဘာတူညီခ်က္ကုိ လက္ခံရန္ လုိသည္။"
  ${LangFileString} MUI_INNERTEXT_LICENSE_BOTTOM_CHECKBOX "အကယ္၍ သင္က သေဘာတူညီခ်က္၏ စည္းကမ္းခ်က္မ်ားကုိ လက္ခံလွ်င္၊ ေအာက္ပါ အကြက္ ကုိ ႏွိပ္ပါ။ သင္သည္ $(^NameDA)ကုိ တပ္ဆင္ရန္အတြက္ သေဘာတူညီခ်က္ကုိ လက္ခံရန္ လုိသည္။ $_CLICK"
  ${LangFileString} MUI_INNERTEXT_LICENSE_BOTTOM_RADIOBUTTONS "သင္က သေဘာတူညီခ်က္၏ စည္းကမ္းခ်က္မ်ားကုိ လက္ခံလွ်င္၊ ေအာက္ပါ ပမထ ေရြးစရာ ကုိ ေရြးပါ။ သင္သည္ $(^NameDA)ကုိ တပ္ဆင္ရန္အတြက္ သေဘာတူညီခ်က္ကုိ လက္ခံရန္ လုိသည္။ $_CLICK"
!endif

!ifdef MUI_UNLICENSEPAGE
  ${LangFileString} MUI_UNTEXT_LICENSE_TITLE "လုိင္စင္ သေဘာတူညီခ်က္"
  ${LangFileString} MUI_UNTEXT_LICENSE_SUBTITLE "ေက်းဇူးျပဳၿပီး $(^NameDA) ကုိ ျပန္မျဖဳတ္မီမွာ လုိင္စင္ စည္းကမ္းခ်က္မ်ားကုိ ဆန္းစစ္ၾကည့္ပါ။"
  ${LangFileString} MUI_UNINNERTEXT_LICENSE_BOTTOM ""အကယ္၍ သင္က သေဘာတူညီခ်က္၏ စည္းကမ္းခ်က္မ်ားကုိ လက္ခံလွ်င္၊ ဆက္လုပ္ရန္ ကြၽႏ္ုပ္ သေဘာတူ ကုိ ႏွိပ္ပါ။ သင္သည္ $(^NameDA) ကုိ ျပန္ျဖဳတ္ရန္အတြက္ သေဘာတူညီခ်က္ကုိ လက္ခံရန္ လုိသည္။"
  ${LangFileString} MUI_UNINNERTEXT_LICENSE_BOTTOM_CHECKBOX "အကယ္၍ သင္က သေဘာတူညီခ်က္၏ စည္းကမ္းခ်က္မ်ားကုိ လက္ခံလွ်င္၊ ေအာက္ပါ အကြက္ကုိ ႏွိပ္ပါ။ သင္သည္ $(^NameDA)ကုိ ျပန္ျဖဳတ္ရန္အတြက္ သေဘာတူညီခ်က္ကုိ လက္ခံရန္ လုိသည္။ $_CLICK"
  ${LangFileString} MUI_UNINNERTEXT_LICENSE_BOTTOM_RADIOBUTTONS "သင္က သေဘာတူညီခ်က္၏ စည္းကမ္းခ်က္မ်ားကုိ လက္ခံလွ်င္၊ ေအာက္ပါ ပမထ ေရြးစရာ ကုိ ေရြးပါ။ သင္သည္ $(^NameDA)ကုိ ျပန္ျဖဳတ္ရန္အတြက္ သေဘာတူညီခ်က္ကုိ လက္ခံရန္ လုိသည္။ $_CLICK"
!endif

!ifdef MUI_LICENSEPAGE | MUI_UNLICENSEPAGE
  ${LangFileString} MUI_INNERTEXT_LICENSE_TOP "သေဘာတူညီခ်က္ တစ္ခုလံုးကုိ ၾကည့္ရန္ ေအာက္ စာမ်က္ႏွာ ကုိ ႏွိပ္ပါ။"
!endif

!ifdef MUI_COMPONENTSPAGE
  ${LangFileString} MUI_TEXT_COMPONENTS_TITLE "အစိတ္အပုိင္းမ်ားကုိ ေရြးပါ"
  ${LangFileString} MUI_TEXT_COMPONENTS_SUBTITLE "$(^NameDA) ထဲက သင္ တပ္ဆင္လုိသည့္ အဂၤါရပ္မ်ားကုိ ေရြးပါ။"
!endif

!ifdef MUI_UNCOMPONENTSPAGE
  ${LangFileString} MUI_UNTEXT_COMPONENTS_TITLE "အစိတ္အပုိင္းမ်ားကုိ ေရြးပါ"
  ${LangFileString} MUI_UNTEXT_COMPONENTS_SUBTITLE "$(^NameDA) ထဲက သင္ ျပန္ျဖဳတ္လုိသည့္ အဂၤါရပ္မ်ားကုိ ေရြးပါ။"
!endif

!ifdef MUI_COMPONENTSPAGE | MUI_UNCOMPONENTSPAGE
  ${LangFileString} MUI_INNERTEXT_COMPONENTS_DESCRIPTION_TITLE "ေဖၚျပခ်က္"
  !ifndef NSIS_CONFIG_COMPONENTPAGE_ALTERNATIVE
    ${LangFileString} MUI_INNERTEXT_COMPONENTS_DESCRIPTION_INFO "ေဖၚျပခ်က္ကုိ ၾကည့္ရန္ အစိတ္အပုိင္း တစ္ခု အေပၚမွာ ေမာက္ဆ္ကုိ ထားေပးပါ။"
  !else
    ${LangFileString} MUI_INNERTEXT_COMPONENTS_DESCRIPTION_INFO "ေဖၚျပခ်က္ကုိ ၾကည့္ရန္ အစိတ္အပုိင္း တစ္ခုကုိ ေရြးပါ။"
  !endif
!endif

!ifdef MUI_DIRECTORYPAGE
  ${LangFileString} MUI_TEXT_DIRECTORY_TITLE "တည္ေနရာ တပ္ဆင္ကုိ ေရြးပါ"
  ${LangFileString} MUI_TEXT_DIRECTORY_SUBTITLE "$(^NameDA) အား တပ္ဆင္လုိသည့္ ဖုိင္တြဲကုိ ေရြးပါ။"
!endif

!ifdef MUI_UNDIRECTORYPAGE
  ${LangFileString} MUI_UNTEXT_DIRECTORY_TITLE "တည္ေနရာ ျပန္ျဖဳတ္ကုိ ေရြးပါ"
  ${LangFileString} MUI_UNTEXT_DIRECTORY_SUBTITLE "$(^NameDA) အား ျပန္ျဖဳတ္ရန္လုိသည့္ ဖုိင္တြဲကုိ ေရြးပါ။"
!endif

!ifdef MUI_INSTFILESPAGE
  ${LangFileString} MUI_TEXT_INSTALLING_TITLE "တပ္ဆင္ေန"
  ${LangFileString} MUI_TEXT_INSTALLING_SUBTITLE "ေက်းဇူးျပဳၿပီး $(^NameDA) ကုိ တပ္ဆင္ေနစဥ္ ေစာင့္ပါ။"
  ${LangFileString} MUI_TEXT_FINISH_TITLE "တပ္ဆင္မႈ ၿပီးဆံုးသြားၿပီ"
  ${LangFileString} MUI_TEXT_FINISH_SUBTITLE "စတင္တပ္ဆင္မႈ ေအာင္ျမင္စြာ ၿပီးဆံုးသြားၿပီ။"
  ${LangFileString} MUI_TEXT_ABORT_TITLE "တပ္ဆင္မႈကုိ ရုပ္သိမ္းလုိက္"
  ${LangFileString} MUI_TEXT_ABORT_SUBTITLE "စတင္တပ္ဆင္မႈ ေအာင္ျမင္စြာ မၿပီးဆံုးခဲ့။"
!endif

!ifdef MUI_UNINSTFILESPAGE
  ${LangFileString} MUI_UNTEXT_UNINSTALLING_TITLE "ျပန္ျဖဳတ္ေန"
  ${LangFileString} MUI_UNTEXT_UNINSTALLING_SUBTITLE "ေက်းဇူးျပဳၿပီး $(^NameDA) ကုိ ျပန္ျဖဳတ္ေနစဥ္ ေစာင့္ပါ။"
  ${LangFileString} MUI_UNTEXT_FINISH_TITLE "ျပန္ျဖဳတ္မႈ ၿပီးဆံုးသြားၿပီ"
  ${LangFileString} MUI_UNTEXT_FINISH_SUBTITLE "ျပန္ျဖဳတ္မႈ ေအာင္ျမင္စြာ ၿပီးဆံုးသြားၿပီ။"
  ${LangFileString} MUI_UNTEXT_ABORT_TITLE "ျပန္ျဖဳတ္မႈ ရုပိသိမ္းလုိက္" 
  ${LangFileString} MUI_UNTEXT_ABORT_SUBTITLE "ျပန္ျဖဳတ္မႈမွာ ေအာင္ျမင္စြာ မၿပီးဆံုးခဲ့။"
!endif

!ifdef MUI_FINISHPAGE
  ${LangFileString} MUI_TEXT_FINISH_INFO_TITLE "$(^NameDA) စတင္တပ္ဆင္မႈကုိ ၿပီးဆံုးေအာင္ ျပဳလုပ္ေန"
  ${LangFileString} MUI_TEXT_FINISH_INFO_TEXT "$(^NameDA) ကုိ သင္၏ကြန္ပ်ဴတာမွာ တပ္ဆင္ၿပီးသြားၿပီ။ $\r$\n$\r$\nစတင္တပ္ဆင္မႈကုိ ပိတ္ရန္ အဆံုးသတ္ ကုိ ႏွိပ္ပါ။"
  ${LangFileString} MUI_TEXT_FINISH_INFO_REBOOT "$(^NameDA)အား တပ္ဆင္မႈ ၿပီးဆံုးရန္အတြက္ သင္၏ ကြန္ပ်ဴတာကုိ ျပန္ဖြင့္ရန္ လုိသည္။ သင္ ယခု ျပန္ဖြင့္လုိသလား?" 
!endif

!ifdef MUI_UNFINISHPAGE
  ${LangFileString} MUI_UNTEXT_FINISH_INFO_TITLE "$(^NameDA) အား ျပႏ္ုျဖဳတ္မႈ   ၿပီးဆံုးေအာင္ ျပဳလုပ္ေန"
  ${LangFileString} MUI_UNTEXT_FINISH_INFO_TEXT "$(^NameDA)ကုိ သင္၏ ကြန္ပ်ဴတာထဲမွ ျပန္ျဖဳတ္ၿပီးသြားၿပီ။ $\r$\n$\r$\nစတင္တပ္ဆင္မႈကုိ ပိတ္ရန္ အဆံုးသတ္ ကုိ ႏွိပ္ပါ။"
  ${LangFileString} MUI_UNTEXT_FINISH_INFO_REBOOT "$(^NameDA) အား ျပန္ျဖဳတ္မႈ ၿပီးဆံုးရန္အတြက္ သင္၏ ကြန္ပ်ဴတာကုိ ျပန္ဖြင့္ရန္ လုိသည္။ သင္ ယခု ျပန္ဖြင့္လုိသလား?"
!endif

!ifdef MUI_FINISHPAGE | MUI_UNFINISHPAGE
  ${LangFileString} MUI_TEXT_FINISH_REBOOTNOW "ယခု ျပန္ဖြင့္ပါ"
  ${LangFileString} MUI_TEXT_FINISH_REBOOTLATER "ကြၽႏ္ုပ္သည္ ေနာက္မွာ လက္ျဖင့္ ျပန္ဖြင့္မည္"
  ${LangFileString} MUI_TEXT_FINISH_RUN "&ဖြင့္ပါ $(^NameDA)"
  ${LangFileString} MUI_TEXT_FINISH_SHOWREADME "ဖတ္&ရန္ကုိ ျပပါ"
  ${LangFileString} MUI_BUTTONTEXT_FINISH "&အဆံုးသတ္"  
!endif

!ifdef MUI_STARTMENUPAGE
  ${LangFileString} MUI_TEXT_STARTMENU_TITLE "စတင္ေရး မီႏူး ဖုိင္တြဲ ကုိ ေရြးပါ"
  ${LangFileString} MUI_TEXT_STARTMENU_SUBTITLE "$(^NameDA) ျဖတ္လမ္းမ်ားအတြက္ စတင္ေရး မီႏူး ဖုိင္တြဲ ကုိ ေရြးပါ။"
  ${LangFileString} MUI_INNERTEXT_STARTMENU_TOP "ပရုိဂရမ္အတြက္ ျဖတ္လမ္းမ်ားကုိ သင္က ဖန္တီးလုိသည့္ စတင္ေရး မီႏူး ဖုိင္တြဲ ကုိ ေရြးပါ။ သင္သည္ ဖုိင္တြဲ အသစ္ တစ္ခုကုိ ဖန္တီးရန္ အမည္ကုိပါ ထည့္ေပးႏုိင္သည္။"
  ${LangFileString} MUI_INNERTEXT_STARTMENU_CHECKBOX "ျဖတ္လမ္းမ်ာကုိ မဖန္တီးပါႏွင့္"
!endif 

!ifdef MUI_UNCONFIRMPAGE
  ${LangFileString} MUI_UNTEXT_CONFIRM_TITLE "$(^NameDA) ကုိ ျပန္ျဖဳတ္ပါ"
  ${LangFileString} MUI_UNTEXT_CONFIRM_SUBTITLE "$(^NameDA) ကုိ သင္၏ကြန္ပ်ဴတာ ထဲမွ ဖယ္ရွားပါ။"
!endif

!ifdef MUI_ABORTWARNING
  ${LangFileString} MUI_TEXT_ABORTWARNING "သင္သည္ $(^Name) အား စတင္တပ္ဆင္မႈကုိ ပိတ္ပစ္ခ်င္တာ ေသခ်ာလား?"
!endif

!ifdef MUI_UNABORTWARNING
  ${LangFileString} MUI_UNTEXT_ABORTWARNING "သင္သည္ $(^Name) အား ျပန္ျဖဳတ္မႈကုိ ပိတ္ပစ္ခ်င္တာ ေသခ်ာလား?"
!endif

!ifdef MULTIUSER_INSTALLMODEPAGE
  ${LangFileString} MULTIUSER_TEXT_INSTALLMODE_TITLE "သံုးစြဲသူမ်ားကုိ ေရြးပါ"
  ${LangFileString} MULTIUSER_TEXT_INSTALLMODE_SUBTITLE "သင္က $(^NameDA) ကုိ သင္တပ္ဆင္ ေပးလုိသည့္ သံုးစြဲသူမ်ားကုိ ေရြးပါ"
  ${LangFileString} MULTIUSER_INNERTEXT_INSTALLMODE_TOP "သင္သည္ $(^NameDA) ကုိ တပ္ဆင္လုိသည္မွာ သင့္ တစ္ေယာက္တည္း အတြက္လား သုိ႔မဟုတ္ ဤကြန္ပ်ဴတာကုိ သံုးစြဲၾကသူ အားလံုးတုိ႔အတြက္လား ေရြးပါ။ $(^ClickNext)"
  ${LangFileString} MULTIUSER_INNERTEXT_INSTALLMODE_ALLUSERS "ဤကြန္ပ်ဴတာကုိ သံုးသူ မည္သူအတြက္မဆုိ တပ္ဆင္ပါ"
  ${LangFileString} MULTIUSER_INNERTEXT_INSTALLMODE_CURRENTUSER "ကြၽႏ္ုပ္အတြက္သာ တပ္ဆင္ပါ"
!endif
