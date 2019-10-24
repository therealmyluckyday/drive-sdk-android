#!/usr/bin/env bash

set -e

if [[ "${DEBUG}" = "true" ]]
then
    set -x
else
    UNZIP_OPTIONS="-q"
fi

ANDROID_SDK_TOOLS=4333796
DOWNLOAD_ANDROID_SDK="android-sdk"
export ANDROID_HOME="${DOWNLOAD_ANDROID_SDK}"
FILE_NAME="android-sdk.zip"
OS_TYPE=$(uname -s | tr '[:upper:]' '[:lower:]')

__download_install_android_sdk() {
  echo "---> Downloading and installing the Android SDK ..."
  echo "----> Downloading the Android SDK ..."
  wget --quiet \
    --output-document="${FILE_NAME}" \
    https://dl.google.com/android/repository/sdk-tools-${OS_TYPE}-${ANDROID_SDK_TOOLS}.zip

  echo "----> Installing the Android SDK ..."
  unzip -d "${DOWNLOAD_ANDROID_SDK}" "${UNZIP_OPTIONS}" ${FILE_NAME}
}

__install_android_platform() {
  echo "---> Installing the Android Platform ..."

  API_LEVEL=$1
  if [[ "${API_LEVEL}" = "" ]]
  then
    echo "Specify the version of API Level"
    exit 1
  fi

  echo y | ${DOWNLOAD_ANDROID_SDK}/tools/bin/sdkmanager "platforms;android-${API_LEVEL}" > /dev/null
  echo y | ${DOWNLOAD_ANDROID_SDK}/tools/bin/sdkmanager "platform-tools" > /dev/null
}

__install_android_build() {
  echo "---> Installing the Android Build ..."

  VERSION=$(${DOWNLOAD_ANDROID_SDK}/tools/bin/sdkmanager --list 2>/dev/null | \
    grep build-tools | \
    grep ${API_LEVEL} | \
    cut -d'|' -f2 | \
    sed -re "s/[[:space:]]//g" | \
    tail -n 1)
  echo "----> Installing the build-tools ${VERSION} .."
  echo y | ${DOWNLOAD_ANDROID_SDK}/tools/bin/sdkmanager "build-tools;${VERSION}" > /dev/null
}

__accept_licenses() {
  echo "---> Accepting the Android licenses ..."

  set +o pipefail
  yes | ${DOWNLOAD_ANDROID_SDK}/tools/bin/sdkmanager --licenses
  set -o pipefail
}

__check_credentials() {
  if [[ "${ARTIFACTORY_USERNAME}" = "" || "${ARTIFACTORY_PASSWORD}" = "" ]]
  then
    echo "You need to provide the ARTIFACTORY_USERNAME and ARTIFACTORY_PASSWORD!"
    exit 1
  fi
}

__assemble_debug() {
  echo "---> Assembling debug ..."
  ./gradlew assembleDebug
}

__clean() {
    echo "--> Cleaning ..."
    test -f "${FILE_NAME}" && echo "---> Deleting ${FILE_NAME} ..." && rm "${FILE_NAME}" || :;

    if [[ -d ${DOWNLOAD_ANDROID_SDK} ]]
    then
        echo "---> Deleting $f ..."
        rm -rf $f
    fi
}

__lint() {
  DIRECTORY=$1
  echo "--> Linting ${DIRECTORY}..."
  ./gradlew :$DIRECTORY:lint
}

__test() {
  echo "--> Testing ..."
  ./gradlew test
}

# Main
__usage() {
  echo "Usage: $0 [ --build ] [ --clean ] [ --env  ] [ --install-sdk ] [ --lint-app  ] [ --lint-sdk  ]" 1>&2
  exit 1
}

if [[ $# -eq 0 ]]
then
  __usage
  exit 1
fi

OPTION=$1
export PATH="${ANDROID_HOME}/platform-tools:${PATH}"
while [[ -n ${OPTION} ]];
do
  case "${OPTION}" in
    --assemble-debug)
      __check_credentials
      __assemble_debug
      break
      ;;
    --clean | -c)
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
    --lint-app)
      __lint app
      break
      ;;
    --lint-sdk)
      __lint sdk
      break
      ;;
    --test)
      __test
      break
      ;;
    *)
      echo "Unknown option: ${OPTION}"
      exit 1
      ;;
  esac
done
