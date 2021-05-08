@echo off
rem This batch file is used to install the maven dependencies needed by the
rem Ardenus Engine but are not present on the central repository.

rem Install Discord Rich Presence 1.6.2
call mvn install:install-file -Dfile=net-arikia-dev-discord-rpc-1.6.2.jar ^
 -DgroupId=net.arikia.dev -DartifactId=discord-rpc -Dversion=1.6.2 ^
 -Dpackaging=jar

pause
