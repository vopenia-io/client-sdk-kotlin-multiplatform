#!/bin/bash

rm -rf appAndroid/build || echo "couldn't clean, folder didn't exist"

./gradlew :appAndroid:assembleRelease || exit 1
./gradlew :appAndroid:appDistributionUploadRelease
