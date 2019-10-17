pipeline {
  agent {
    docker {
      image 'openjdk:8-jdk'
      args '-v /var/run/docker.sock:/var/run/docker.sock'
    }
  }

  options {
    parallelsAlwaysFailFast()
    buildDiscarder(logRotator(daysToKeepStr: '10',numToKeepStr: '20'))
  }

  environment {
    GIT_COMMITTER_FULL_NAME = sh(script: "git show -s --pretty=%an",, returnStdout: true).trim()
    GIT_COMMITTER_EMAIL = sh(script: "git show -s --pretty=%ae",, returnStdout: true).trim()
    GITHUB_TOKEN = credentials('jenkins_pipeline')
    SLACK_USER_TOKEN = credentials('slack_user_token')
    ARTIFACTORY_CREDENTIALS = credentials('axa_artifactory')
    ARTIFACTORY_USERNAME = "${ARTIFACTORY_CREDENTIALS_USR}"
    ARTIFACTORY_PASSWORD = "${ARTIFACTORY_CREDENTIALS_PSW}"
    API_LEVEL = "28"
    HOME = "${WORKSPACE}/home"
  }

  stages {
    stage('env') {
      steps {
        sh 'env | sort'
        sh 'mount'
      }
    }

    stage('temporary home') {
      steps {
        sh 'mkdir -p ${HOME}'
      }
    }

    stage('install-sdk') {
      steps {
        sh './make.sh --install-sdk ${API_LEVEL}'
      }
    }

    stage('assembleDebug') {
      steps {
        sh './make.sh --assemble-debug'
      }
    }

    stage('test') {
      steps {
        sh './make.sh --test'
      }
    }

    stage('lint-app') {
      steps {
        sh './make.sh --lint-app'
      }
    }

    stage('lint-sdk') {
      steps {
        sh './make.sh --lint-sdk'
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: '**/build/reports/**'
    }
  }

}
