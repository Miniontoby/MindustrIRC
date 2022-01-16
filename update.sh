#!/bin/ksh
./gradlew jar || exit 1
cp ./build/libs/mindustrircDesktop.jar ~/mindustry/config/mods || exit 1
