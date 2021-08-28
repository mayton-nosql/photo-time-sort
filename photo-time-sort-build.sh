#!/bin/bash -e

mkdir -p bin
mvn package
cp -f src/main/resources/photo-time-sort.sh ./bin
cp -f src/main/resources/photo-time-sort.cmd ./bin
cp -f target/photo-time-sort.jar ./bin/photo-time-sort.jar

RELEASE_TAG=1.1-alpha

cd bin

rm -f  photo-time-sort-$RELEASE_TAG.zip
zip -0 photo-time-sort-$RELEASE_TAG.zip photo-time-sort.*

cd ..

