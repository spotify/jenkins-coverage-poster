def call(String htmlPath="target/site/jacoco/index.html", Integer threshold=70) {
  postCoverage(getCoverageFromJacoco(htmlPath))
}
