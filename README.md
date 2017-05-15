![](https://img.shields.io/badge/development%20status-alpha-yellow.svg) [![Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

A Jenkins 2.0 Pipeline library for automatically posting code coverage results to GitHub pull requests.

Currently supports:
 * input
   * jacoco xml
 * output
   * github.com
   * github enterprise

## Example usage
In your Jenkinsfile:
```
@Library('github.com/spotify/jenkins-coverage-poster@1.0') _

stage("Run tests") {
  sh "mvn test"
}

stage("Post coverage") {
  postJacocoCoverage(threshold: 75)
}
```

## Example result

<img src="./coverage_pass.png" width="700" />

## Parameters
 * `threshold`: your code coverage must be at or above this level to get a passing score
 * `deltaThreshold` (optional): your code coverage must have changed by at least this much to get a passing score
   * if not specified, coverage delta will not be computed and pull request status will not be affected
   * for example, setting `deltaThreshold: -1.0` will cause the pull request to be marked red if your changes result in a drop of more than 1% coverage (as compared to the coverage in the master branch)
 * `xmlPath` (optional): the location of the jacoco coverage xml file
   * defaults to `target/site/jacoco/jacoco.xml`

## What is "coverage"?
This library uses total *instruction* coverage for all code coverage calculations. Note that this is different from line coverage and branch coverage.

## Requirements
 * jenkins 2.0+
 * bash 4.0+
 * python 2.6+
 * maven, sbt, or something else that can generate jacoco reports
 
In addition, in Jenkins there must be a defined Credential called `github-user-token`, containing as password a valid GitHub api token. The Credential username can be anything, as it is not used. The token's permissions must allow read access to repositories and posting comments to pull requests at minimum.

## Code of conduct
This project adheres to the [Open Code of Conduct][code-of-conduct]. By participating, you are expected to honor this code.

[code-of-conduct]: https://github.com/spotify/code-of-conduct/blob/master/code-of-conduct.md
