#!/bin/ksh
if [[ "x$1" == "x" ]]; then
	FILE="MindustrIRC"
else
	FILE="$1"
fi

nano src/com/miniontoby/MindustrIRC/$FILE.java
