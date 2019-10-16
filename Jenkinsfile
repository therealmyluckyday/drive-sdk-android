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
    API_LEVEL = "28"
    HOME = "/tmp/"
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
        sh './make.sh --install-sdk ${API_LEVEL}'
      }
    }

    stage('build') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'axa_artifactory', usernameVariable: 'ARTIFACTORY_USERNAME', passwordVariable: 'ARTIFACTORY_PASSWORD')]) {
            sh './make.sh --build'
        }
      }
    }

  }

  post {
    always {
      archiveArtifacts artifacts: '**/build/reports/**'
    }
  }

}
