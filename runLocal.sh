#!/bin/sh
# build the java files.
# there will eventually be a separate build step, but for now the build counts against your time.



#javac src/*.java -classpath /battlecode-java:. -d /battlecode-java:.



#javac src/*.java -verbose  -classpath /battlecode-java:. -d /battlecode-java:.
#java -classpath /battlecode-java:. Player 





javac src/Bruteforcer/*.java -classpath "battlecode.jar;/battlecode-java:.;" -d "out"
java -classpath "out;battlecode.jar" "Bruteforcer.Player"
