@echo off
cls
echo Verifying JAR Signature ...
echo .
if not "%1" == "" goto next1
echo Missing jar file parameter
goto out
:next1
c:\martus\bin\java -cp .;..\Martus\martus.jar org.martus.jarverifier.JarVerifier %1
:out
echo .
pause

