#!/bin/ksh
if [[ "x$1" == "x" ]]; then
	FILE="MinDiscory"
else
	FILE="$1"
fi

nano src/com/miniontoby/MinDiscory/$FILE.java
