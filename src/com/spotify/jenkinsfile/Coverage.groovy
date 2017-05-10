#!/usr/bin/env groovy
package com.spotify.jenkinsfile

def getCoverageFromJacoco(String htmlPath) {
  if(!fileExists(htmlPath)) {
    echo "[WARNING] Jacoco coverage report not found at ${htmlPath}"
    return null
  }

  // can't use String.replaceAll() with groups: https://issues.jenkins-ci.org/browse/JENKINS-26481
  withEnv(["HTML_PATH=${htmlPath}"]) {
    final coverage = sh(returnStdout: true, script: '''#!/bin/bash -xe
      perl -pe "s|.*<td>Total</td>.*?>([0-9]+%)</td>.*|\\1|" "${HTML_PATH}"
    ''')
    return coverage.replaceAll(/\%$/, "") as Integer
  }
}

def postCoverage(Integer coverage, Integer threshold) {
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'github-user-token',
                    usernameVariable: 'NOT_USED', passwordVariable: 'TOKEN']]) {
    if(coverage == null || coverage == "") {
      echo "[WARNING] No coverage to post"
      return
    }

    final state = (coverage >= threshold) ? "success" : "failure"
    final commitHash = getCommitHash()

    withEnv(["COVERAGE=${coverage}", "THRESHOLD=${threshold}", "STATE=${state}", "COMMIT_HASH=${commitHash}"]) {
      sh '''#!/bin/bash -xe
        GITHUB_HOST=$(git config remote.origin.url | cut -d/ -f3)
        GITHUB_API_URL=$([[ "${GITHUB_HOST}" == "github.com" ]] && echo "api.github.com" || echo "${GITHUB_HOST}/api/v3")
        ORG_REPO_BRANCH_ARRAY=(${JOB_NAME//// })
        ORG=${ORG_REPO_BRANCH_ARRAY[0]}
        REPO=${ORG_REPO_BRANCH_ARRAY[1]}
        TOKEN_PARAM="access_token=$TOKEN"
        COMMIT_STATUS_URL=$(echo "https://${GITHUB_API_URL}/repos/${ORG}/${REPO}/statuses/${COMMIT_HASH}")

        curl -isSL -X POST "${COMMIT_STATUS_URL}?${TOKEN_PARAM}" -d '{
          "state": "'${STATE}'",
          "target_url": "'${BUILD_URL}'",
          "context": "continuous-integration/jenkins/code-coverage",
          "description": "'${COVERAGE}'% (threshold: '${THRESHOLD}'%)"
        }'
      '''
    }
  }
}

def getCommitHash(String branch) {
  if(branch == null || branch == "") {
    branch = "HEAD"
  }

  withEnv(["BRANCH=${branch}"]) {
    return sh(returnStdout: true, script: '''#!/bin/bash -xe
      git rev-parse "$BRANCH"
    ''')
  }
}

