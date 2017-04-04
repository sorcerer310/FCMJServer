#!/usr/bin/env bash
echo "begin deploy"

gitrepo=/soft/MahjongServer
serverpath=/opt/hall/SFS2X

gradle -b $gitrepo/rc-gamehall/build.gradle clean
gradle -b $gitrepo/rc-gamehall/build.gradle build -x test

if [ $? != "0" ]; then
  echo "compile fail"
  exit 1
fi

cp $gitrepo/rc-gamehall/build/libs/*.jar $serverpath/extensions/hall/
cp $gitrepo/rc-gamehall/libs/* $serverpath/extensions/hall/

echo "deploy end"
