#!/bin/bash

./gradlew publishToMavenLocal || exit 1 
./gradlew publishAllPublicationsToSonatypeRepository closeAndReleaseSonatypeStagingRepository
