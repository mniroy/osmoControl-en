#!/bin/sh

set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname "$0")" && pwd)
WRAPPER_PROPS="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"
CACHE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/manual"
DIST_URL=$(sed -n 's/^distributionUrl=//p' "$WRAPPER_PROPS" | sed 's#\\##g')
DIST_NAME=$(basename "$DIST_URL" .zip)
DIST_DIR="$CACHE_DIR/$DIST_NAME"
GRADLE_BIN="$DIST_DIR/bin/gradle"

if command -v gradle >/dev/null 2>&1; then
  exec gradle -p "$APP_HOME" "$@"
fi

if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$CACHE_DIR"
  ZIP_FILE="$CACHE_DIR/$DIST_NAME.zip"
  if [ ! -f "$ZIP_FILE" ]; then
    curl -fsSL "$DIST_URL" -o "$ZIP_FILE"
  fi
  rm -rf "$DIST_DIR"
  unzip -q "$ZIP_FILE" -d "$CACHE_DIR"
fi

exec "$GRADLE_BIN" -p "$APP_HOME" "$@"
