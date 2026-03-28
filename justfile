default:
    @just --list

help:
    @just --list

test-protocol:
    ./gradlew --no-daemon :core-protocol:test

test:
    ./gradlew --no-daemon test

lint:
    ./gradlew --no-daemon lint

assemble-debug:
    ./gradlew --no-daemon assembleDebug

install-debug:
    ./gradlew --no-daemon installDebug
