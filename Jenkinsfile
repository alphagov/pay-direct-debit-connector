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
        sh 'mvn clean verify'
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
          buildAppWithMetrics {
            app = "directdebit-connector"
          }
        }
      }
      post {
        failure {
          postMetric("directdebit-connector.docker-build.failure", 1, "new")
        }
      }
    }
    stage('Test') {
      steps {
        runParameterisedEndToEnd("directdebitconnector", null, "end2end-tagged", false, false, "uk.gov.pay.endtoend.categories.End2EndDirectDebit", "", "run-end-to-end-direct-debit-tests")
      }
    }
    stage('Docker Tag') {
      steps {
        script {
          dockerTagWithMetrics {
            app = "directdebit-connector"
          }
        }
      }
      post {
        failure {
          postMetric("directdebit-connector.docker-tag.failure", 1, "new")
        }
      }
    }
    stage('Deploy') {
      when {
        branch 'master'
      }
      steps {
        deployEcs("directdebit-connector", "test", null, true, true, "uk.gov.pay.endtoend.categories.SmokeDirectDebitPayments", false)
      }
    }
  }
  post {
    failure {
      postMetric("directdebit-connector.failure", 1, "new")
    }
    success {
      postSuccessfulMetrics("directdebit-connector")
    }
  }
}
