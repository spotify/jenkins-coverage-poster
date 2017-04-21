A Jenkins Pipeline library for posting code coverage results.

Currently only supports jacoco html.

Example usage in your Jenkinsfile:
```
@Library('github.com/mpavlov/test-jenkins-lib') _

stage("Post coverage") {
  postJacocoCoverage(threshold: 85)
}
```
