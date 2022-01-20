#!/bin/ksh
./gradlew jar || exit 1
cp ./build/libs/mindiscory.jar ~/mindustry/config/mods || exit 1
