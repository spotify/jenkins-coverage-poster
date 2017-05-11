#!/usr/bin/env groovy
package com.spotify.jenkinsfile

def Double getCoverageFromJacoco(String xmlPath) {
  if(!fileExists(xmlPath)) {
    echo "[WARNING] Jacoco coverage report not found at ${xmlPath}"
    return null
  }

  // can't use String.replaceAll() with groups: https://issues.jenkins-ci.org/browse/JENKINS-26481
  withEnv(["XML_PATH=${xmlPath}"]) {
    final coverage = sh(returnStdout: true, script: '''#!/bin/bash -xe
      cat ${XML_PATH} | python -c 'import sys
import xml.etree.ElementTree as ET

tree = ET.parse(sys.stdin)
root = tree.getroot()
for counter in root.findall("counter"):
    if counter.attrib["type"] == "INSTRUCTION":
        missed = float(counter.attrib["missed"])
        covered = float(counter.attrib["covered"])
        coverage = covered / (missed + covered)
        print "%.2f" % (coverage * 100)\'
    ''')

    if(coverage == "") {
      echo "[WARNING] Unable to parse Jacoco coverage report at ${xmlPath}"
      return null
    }

    return coverage as Double
  }
}

def postCoverage(Double coverage, Double threshold) {
  if(coverage == null || coverage == "") {
    echo "[WARNING] No coverage to post"
    return
  }

  final state = (coverage >= threshold) ? "success" : "failure"
  final context = "code-coverage"
  final description = "${coverage}% (threshold: ${threshold}%)"
  postCommitStatus(state, context, description)
}

@NonCPS
def postCoverageDelta(Double coverageDelta, Double threshold) {
  if(threshold == null) {
    echo "[WARNING] No delta threshold specified. Nothing will be posted"
    return
  }

  if(coverageDelta == null || coverageDelta == "") {
    echo "[WARNING] No coverage diff to post"
    return
  }

  final maybePlus = (coverageDelta > 0) ? "+" : ""
  final state = (coverageDelta >= threshold) ? "success" : "failure"
  final context = "code-coverage-delta"
  final groovy.lang.GString description = "${maybePlus}${coverageDelta}% (threshold: ${threshold}%)"
  postCommitStatus(state, context, description)
}

def Double getCoverageDelta() {
  masterCoverage = getCoverage(getCommitHash("origin/master"))
  branchCoverage = getCoverage(getCommitHash())
  return new Double(branchCoverage - masterCoverage).round(2)
}

def Double getCoverage(String commitHash) {
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'github-user-token',
                    usernameVariable: 'NOT_USED', passwordVariable: 'TOKEN']]) {

    final coverage = sh(returnStdout: true, script: """#!/bin/bash -xe
      GITHUB_HOST=\$(git config remote.origin.url | cut -d/ -f3)
      GITHUB_API_URL=\$([[ "\${GITHUB_HOST}" == "github.com" ]] && echo "api.github.com" || echo "\${GITHUB_HOST}/api/v3")
      ORG_REPO_BRANCH_ARRAY=(\${JOB_NAME//// })
      ORG=\${ORG_REPO_BRANCH_ARRAY[0]}
      REPO=\${ORG_REPO_BRANCH_ARRAY[1]}
      TOKEN_PARAM="access_token=\$TOKEN"
      COMMIT_STATUS_URL=\$(echo "https://\${GITHUB_API_URL}/repos/\${ORG}/\${REPO}/commits/${commitHash}/status")

      COMMIT_JSON=\$(curl "\${COMMIT_STATUS_URL}?\${TOKEN_PARAM}")
      
      echo \$COMMIT_JSON | python -c 'import sys, json

content = json.load(sys.stdin)
for status in content[\"statuses\"]:
    if status[\"context\"] == \"code-coverage\":
        print status[\"description\"].split(\"%\")[0]
        sys.exit(0)'
      """)

    if(coverage == "") {
      echo "[WARNING] No coverage found for commit ${commitHash}"
      return 0
    }

    return coverage as Double
  }
}

def postCommitStatus(String state, String context, String description) {
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'github-user-token',
                    usernameVariable: 'NOT_USED', passwordVariable: 'TOKEN']]) {
    final commitHash = getCommitHash()

    // yay, escaping! https://gist.github.com/Faheetah/e11bd0315c34ed32e681616e41279ef4
    final script = """#!/bin/bash -xe
      GITHUB_HOST=\$(git config remote.origin.url | cut -d/ -f3)
      GITHUB_API_URL=\$([[ "\${GITHUB_HOST}" == "github.com" ]] && echo "api.github.com" || echo "\${GITHUB_HOST}/api/v3")
      ORG_REPO_BRANCH_ARRAY=(\${JOB_NAME//// })
      ORG=\${ORG_REPO_BRANCH_ARRAY[0]}
      REPO=\${ORG_REPO_BRANCH_ARRAY[1]}
      TOKEN_PARAM="access_token=\${TOKEN}"
      COMMIT_STATUS_URL=\$(echo "https://\${GITHUB_API_URL}/repos/\${ORG}/\${REPO}/statuses/${commitHash}")

      curl -isSL -X POST "\${COMMIT_STATUS_URL}?\${TOKEN_PARAM}" -d '{
        \"state\": \"${state}\",
        \"target_url\": \"'\${BUILD_URL}'\",
        \"context\": \"${context}\",
        \"description\": \"${description}\"
      }'
    """
    sh script
  }
}

def String getCommitHash(String branch=null) {
  if(branch == null || branch == "") {
    branch = "HEAD"
  }

  withEnv(["BRANCH=${branch}"]) {
    return sh(returnStdout: true, script: '''#!/bin/bash -xe
      git rev-parse "$BRANCH"
    ''').trim()
  }
}

