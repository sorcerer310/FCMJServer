#!/usr/bin/env bash
echo "begin deploy"

gitrepo=/soft/MahjongServer
serverpath=/opt/game/SFS2X

gradle -b $gitrepo/rc-tcpserver/build.gradle clean
gradle -b $gitrepo/rc-tcpserver/build.gradle build -x test

if [ $? != "0" ]; then
  echo "compile fail"
  exit 1
fi

cp $gitrepo/rc-tcpserver/build/libs/*.jar $serverpath/extensions/__lib__/
cp $gitrepo/rc-tcpserver/libs/* $serverpath/extensions/__lib__/
cp $gitrepo/rc-tcpserver/conf/template/* $serverpath/template/

echo "deploy end"
