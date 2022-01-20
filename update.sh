#!/bin/ksh
./gradlew jar || exit 1
cp ./build/libs/mindustrirc.jar ~/mindustry/config/mods || exit 1
