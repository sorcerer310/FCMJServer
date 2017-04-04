@echo off
set SFS_HOME="D:\work\MJ\trunk\server\game\SmartFoxServer_2X_2.8.5\SFS2X"
set RAFO_HOME="D:\work\MJ\trunk\server\MahjongServer"

call gradle -b %RAFO_HOME%\rc-tcpserver\build.gradle clean
call gradle -b %RAFO_HOME%\rc-tcpserver\build.gradle build -x test

copy %RAFO_HOME%\rc-tcpserver\build\libs\*.jar %SFS_HOME%\extensions\__lib__