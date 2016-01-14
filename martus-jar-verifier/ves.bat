@echo off
cls
echo Verificando validez del archivo JAR ...
echo .
if not "%1" == "" goto next1
echo Falta indicar el archivo a verificar
goto out
:next1
c:\martus\bin\java -cp .;..\Martus\martus.jar org.martus.jarverifier.JarVerifier %1
:out
echo .
pause

