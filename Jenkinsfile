#!/usr/bin/env groovy

pipeline {
  agent any

  parameters {
    booleanParam(defaultValue: false, description: '', name: 'runEndToEndOnPR')
  }

  options {
    ansiColor('xterm')
    timestamps()
  }

  libraries {
    lib("pay-jenkins-library@master")
  }

  environment {
    DOCKER_HOST = "unix:///var/run/docker.sock"
    RUN_END_TO_END_ON_PR = "${params.runEndToEndOnPR}"
  }

  stages {
    stage('Maven Build') {
      when {
        not {
          branch 'master'
        }
      }
      steps {
        sh 'mvn clean package'
      }
    }
    stage('Maven Build Without Tests') {
      when {
        branch 'master'
      }
      steps {
        sh 'mvn -Dmaven.test.skip=true clean package'
      }
    }
    stage('Docker Build') {
      steps {
        script {
          buildApp{
            app = "directdebit-connector"
          }
        }
      }
    }
    stage('Docker Tag') {
      steps {
        script {
          dockerTag {
            app = "directdebit-connector"
          }
        }
      }
    }
    stage('Deploy') {
      when {
        branch 'master'
      }
      steps {
        deployEcs("directdebit-connector", "test", null, true, true)
      }
    }
  }
}
