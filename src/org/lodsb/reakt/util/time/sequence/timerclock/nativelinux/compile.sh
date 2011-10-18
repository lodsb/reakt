#!/bin/bash

swig -java -package util.time.sequence.timerclock.nativelinux linuxclock.i
gcc -fpic -c linuxclock.c linuxclock_wrap.c -I /usr/lib/jvm/java-6-sun/include/ -I /usr/lib/jvm/java-6-sun/include/linux
gcc -shared -o linuxclock.so  linuxclock.o linuxclock_wrap.o

