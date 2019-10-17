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
    API_LEVEL = "28"
    HOME = "${WORKSPACE}"
  }

  stages {
    stage('env') {
      steps {
        sh 'env | sort'
        sh 'mount'
      }
    }

    stage('install-sdk') {
      steps {
        sh 'cd ${HOME} ; ./make.sh --install-sdk ${API_LEVEL}'
      }
    }

    stage('lint') {
      parallel {
        stage('app') {
          stages {
            stage('lint') {
              steps {
                sh 'cd ${HOME} ; ./make.sh --lint-app'
              }
            }
          }
        }
        stage('sdk') {
          stages {
            stage('lint') {
              steps {
                sh 'cd ${HOME} ; ./make.sh --lint-sdk'
              }
            }
          }

        }
      }
    }

    stage('build') {
      steps {
        sh 'cd ${HOME} ; ARTIFACTORY_USERNAME=env.ARTIFACTORY_CREDENTIALS_USR ARTIFACTORY_PASSWORD=env.ARTIFACTORY_CREDENTIALS.PSW ./make.sh --build'
      }
    }

  }

  post {
    always {
      archiveArtifacts artifacts: '**/build/reports/**'

      cleanWs()
    }
  }

}
