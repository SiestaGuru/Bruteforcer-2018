#!/bin/sh
# build the java files.
# there will eventually be a separate build step, but for now the build counts against your time.



#javac src/*.java -classpath /battlecode-java:. -d /battlecode-java:.
javac *.java -classpath /battlecode-java:. -d ./battlecode-java:.


echo "test1"

for entry in "$search_dir"/*
do
  echo "$entry"
done

echo "test2"

for entry in "$search_dir"/src/*
do
  echo "$entry"
done

echo "test3"

for entry in "$search_dir"/battlecode-java/*
do
  echo "$entry"
done

echo "test4"


for entry in "$search_dir"/battlecode/*
do
  echo "$entry"
done

echo "test5"


for entry in "$search_dir"/battlecode-java/bc/*
do
  echo "$entry"
done


java -verbose -classpath /battlecode-java:. Player 
