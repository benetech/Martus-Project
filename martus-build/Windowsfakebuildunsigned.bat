cd D:\temp\martus
call buildr --trace -f martus-build/buildfile clean martus-client:build_unsigned test=no

cd D:\temp\martus\martus-client\target
mkdir D:\temp\martus\martus-client\builds\TEST

copy *.zip D:\Temp\martus\martus-client\builds\TEST
pause
pause