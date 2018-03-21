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
        script {
          def long stepBuildTime = System.currentTimeMillis()

          sh 'mvn clean verify'
          postSuccessfulMetrics("directdebit-connector.maven-build", stepBuildTime)
        }
      }
      post {
        failure {
          postMetric("directdebit-connector.maven-build.failure", 1)
        }
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
          postMetric("directdebit-connector.docker-build.failure", 1)
        }
      }
    }
    stage('Direct-Debit End-to-End') {
      steps {
        runDirectDebitE2E("directdebitconnector")
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
          postMetric("directdebit-connector.docker-tag.failure", 1)
        }
      }
    }
    stage('Deploy') {
      when {
        branch 'master'
      }
      steps {
        deployEcs("directdebit-connector", "test", null, false, false, "dummy", false)
      }
    }
    stage('Smoke Tests') {
      when {
        branch 'master'
      }
      steps {
        runDirectDebitSmokeTest()
      }
    }
    stage('Complete') {
      failFast true
      parallel {
        stage('Tag Build') {
          when {
            branch 'master'
          }
          steps {
            tagDeployment("direct-debit-connector")
          }
        }
        stage('Trigger Deploy Notification') {
          when {
            branch 'master'
          }
          steps {
            triggerGraphiteDeployEvent("direct-debit-connector")
          }
        }
      }
    }
  }
  post {
    failure {
      postMetric(appendBranchSuffix("directdebit-connector") + ".failure", 1)
    }
    success {
      postSuccessfulMetrics(appendBranchSuffix("directdebit-connector"))
    }
  }
}
