#!/usr/bin/env bash

set -ex

if [[ $# -eq 0 ]]
then
  echo "Specify the version of API Level"
  exit 1
fi

API_LEVEL=$1
ANDROID_SDK_TOOLS=4333796
OS_TYPE=$(uname -s | tr '[:upper:]' '[:lower:]')

__download_install_android_sdk() {
  echo "--> Downloading and installing the Android SDK ..."
  FILE_NAME="android-sdk.zip"

  echo "---> Downloading the Android SDK ..."
  wget --quiet \
    --output-document="${FILE_NAME}" \
    https://dl.google.com/android/repository/sdk-tools-${OS_TYPE}-${ANDROID_SDK_TOOLS}.zip

  echo "---> Installing the Android SDK ..."
  unzip ${FILE_NAME}
}

__install_android_platform() {
  echo "--> Installing the Android Platform ..."

  cd tools/
  echo y | ./bin/sdkmanager "platforms;android-${API_LEVEL}" > /dev/null
  echo y | ./bin/sdkmanager "platform-tools" > /dev/null
  cd - > /dev/null
}

__install_android_build() {
  echo "--> Installing the Android Build ..."

  cd tools/
  VERSION=$(./bin/sdkmanager --list 2>/dev/null | \
    grep build-tools | \
    grep ${API_LEVEL} | \
    cut -d'|' -f2 | \
    sed -re "s/[[:space:]]//g" | \
    tail -n 1)
  echo "---> Installing the build-tools ${VERSION} .."
  echo y | ./bin/sdkmanager "build-tools;${VERSION}" > /dev/null
  cd - > /dev/null
}


# Main
__download_install_android_sdk
__install_android_platform
__install_android_build

