rem mkdir D:\temp
rem mkdir D:\temp\martus
rd /s /q D:\temp\martus

xcopy /S /K D:\Benetech\martus-build D:\temp\martus\martus-build\
xcopy /S /K D:\Benetech\martus-amplifier D:\temp\martus\martus-amplifier\
xcopy /S /K D:\Benetech\martus-bc-jce D:\temp\martus\martus-bc-jce\
xcopy /S /K D:\Benetech\martus-client D:\temp\martus\martus-client\
xcopy /S /K D:\Benetech\martus-clientside D:\temp\martus\martus-clientside\
xcopy /S /K D:\Benetech\martus-common D:\temp\martus\martus-common\
xcopy /S /K D:\Benetech\martus-docs D:\temp\martus\martus-docs\
xcopy /S /K D:\Benetech\martus-hrdag D:\temp\martus\martus-hrdag\
xcopy /S /K D:\Benetech\martus-jar-verifier D:\temp\martus\martus-jar-verifier\
xcopy /S /K D:\Benetech\martus-js-xml-generator D:\temp\martus\martus-js-xml-generator\
xcopy /S /K D:\Benetech\martus-logi D:\temp\martus\martus-logi\
xcopy /S /K D:\Benetech\martus-meta D:\temp\martus\martus-meta\
xcopy /S /K D:\Benetech\martus-mspa D:\temp\martus\martus-mspa\
xcopy /S /K D:\Benetech\martus-server D:\temp\martus\martus-server\
xcopy /S /K D:\Benetech\martus-swing D:\temp\martus\martus-swing\
xcopy /S /K D:\Benetech\martus-thirdparty D:\temp\martus\martus-thirdparty\
xcopy /S /K D:\Benetech\martus-utils D:\temp\martus\martus-utils\

pause
