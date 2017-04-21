![](https://img.shields.io/badge/development%20status-alpha-yellow.svg) [![Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

A Jenkins Pipeline library for posting code coverage results.

Currently only supports jacoco html.

## Example usage
Adding this to your Jenkinsfile:
```
@Library('github.com/mpavlov/test-jenkins-lib') _

stage("Post coverage") {
  postJacocoCoverage(threshold: 75)
}
```

... gives you this in your Pull Request:

<img src="./coverage_pass.png" width="700" />

## Code of conduct
This project adheres to the [Open Code of Conduct][code-of-conduct]. By participating, you are expected to honor this code.

[code-of-conduct]: https://github.com/spotify/code-of-conduct/blob/master/code-of-conduct.md
