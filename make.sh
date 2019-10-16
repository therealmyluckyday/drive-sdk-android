#!/usr/bin/env bash

set -e

if [[ "${DEBUG}" = "true" ]]
then
    set -x
else
    UNZIP_OPTIONS="-q"
fi

ANDROID_SDK_TOOLS=4333796
FILE_NAME="android-sdk.zip"
ANDROID_HOME="${PWD}/android-sdk"
OS_TYPE=$(uname -s | tr '[:upper:]' '[:lower:]')

__download_install_android_sdk() {
  echo "---> Downloading and installing the Android SDK ..."
  echo "----> Downloading the Android SDK ..."
  wget --quiet \
    --output-document="${FILE_NAME}" \
    https://dl.google.com/android/repository/sdk-tools-${OS_TYPE}-${ANDROID_SDK_TOOLS}.zip

  echo "----> Installing the Android SDK ..."
  unzip -d "${ANDROID_HOME}" "${UNZIP_OPTIONS}" ${FILE_NAME}
}

__install_android_platform() {
  echo "---> Installing the Android Platform ..."

  API_LEVEL=$1
  if [[ "${API_LEVEL}" = "" ]]
  then
    echo "Specify the version of API Level"
    exit 1
  fi

  cd "${ANDROID_HOME}"
  echo y | ./tools/bin/sdkmanager "platforms;android-${API_LEVEL}" > /dev/null
  echo y | ./tools/bin/sdkmanager "platform-tools" > /dev/null
  cd - > /dev/null
}

__install_android_build() {
  echo "---> Installing the Android Build ..."

  cd "${ANDROID_HOME}"
  VERSION=$(./tools/bin/sdkmanager --list 2>/dev/null | \
    grep build-tools | \
    grep ${API_LEVEL} | \
    cut -d'|' -f2 | \
    sed -re "s/[[:space:]]//g" | \
    tail -n 1)
  echo "----> Installing the build-tools ${VERSION} .."
  echo y | ./tools/bin/sdkmanager "build-tools;${VERSION}" > /dev/null
  cd - > /dev/null
}

__accept_licenses() {
  echo "---> Accepting the Android licenses ..."

  cd "${ANDROID_HOME}"
  set +o pipefail
  yes | ./tools/bin/sdkmanager --licenses
  set -o pipefail
  cd - > /dev/null
}

__check_credentials() {
  if [[ "${ARTIFACTORY_USERNAME}" = "" || "${ARTIFACTORY_PASSWORD}" = "" ]]
  then
    echo "You need to provide the ARTIFACTORY_USERNAME and ARTIFACTORY_PASSWORD!"
    exit 1
  fi
}

__build() {
  echo "---> Building ..."
  PATH="${ANDROID_HOME}/platform-tools:${PATH}" ./gradlew build
}

__clean() {
    test -f "${FILE_NAME}" && echo "---> Deleting ${FILE_NAME} ..." && rm "${FILE_NAME}" || :;

    for f in build build-tools licenses platforms platform-tools tools
    do
        if [[ -d $f ]]
        then
            echo "---> Deleting $f ..."
            rm -rf $f
        fi
    done
}

# Main
__usage() {
  echo "Usage: $0 [ --build ] [ --clean ] [ --env  ] [ --install-sdk ]" 1>&2
  exit 1
}

if [[ $# -eq 0 ]]
then
  __usage
  exit 1
fi

OPTION=$1
while [[ -n ${OPTION} ]];
do
  case "${OPTION}" in
    --build)
      __check_credentials
      __build
      break
      ;;
    --clean | -c)
      echo "--> Cleaning ..."
      __clean
      break
      ;;
    --env)
      echo "--> Display all environment variables ..."
      env | sort
      break
      ;;
    --install-sdk)
      echo "--> Installing ..."
      shift
      __download_install_android_sdk
      __install_android_platform $1
      __install_android_build
      __accept_licenses
      break
      ;;
    *)
      echo "Unknown option: ${OPTION}"
      exit 1
      ;;
  esac
done
