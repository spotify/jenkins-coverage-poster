def call(String htmlPath="target/site/jacoco/index.html") {
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
