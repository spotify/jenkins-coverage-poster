def call(Integer threshold, String htmlPath="target/site/jacoco/index.html") {
  postCoverage(getCoverageFromJacoco(htmlPath), threshold)
}
