def call(coverage, Integer threshold=70) {
  withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'github-user-token',
                    usernameVariable: 'NOT_USED', passwordVariable: 'TOKEN']]) {
    if(coverage == "") {
      echo "[WARNING] No coverage to post"
      return
    }

    final state = (coverage >= threshold) ? "success" : "failure"

    withEnv(["COVERAGE=${coverage}", "THRESHOLD=${threshold}", "STATE=${state}"]) {
      sh '''#!/bin/bash -xe
        GITHUB_HOST=$(git config remote.origin.url | cut -d/ -f3)
        GITHUB_API_URL=$([[ "${GITHUB_HOST}" == "github.com" ]] && echo "api.github.com" || echo "${GITHUB_HOST}/api/v3")
        ORG_REPO_BRANCH_ARRAY=(${JOB_NAME//// })
        ORG=${ORG_REPO_BRANCH_ARRAY[0]}
        REPO=${ORG_REPO_BRANCH_ARRAY[1]}
        TOKEN_PARAM="access_token=$TOKEN"
        SHA=$(git rev-parse HEAD)
        COMMIT_STATUS_URL=$(echo "${GITHUB_API_URL}/repos/${ORG}/${REPO}/statuses/${SHA}")

        curl -sS -X POST "${COMMIT_STATUS_URL}?${TOKEN_PARAM}" -d '{
          "state": "'${STATE}'",
          "target_url": "'${BUILD_URL}'",
          "context": "continuous-integration/jenkins/code-coverage",
          "description": "'${COVERAGE}'% (threshold: '${THRESHOLD}'%)"
        }'
      '''
    }
  }
}
