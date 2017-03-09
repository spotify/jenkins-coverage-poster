def call(Integer threshold, String htmlPath="target/site/jacoco/index.html") {
  def util = new com.mpavlov.jenkinsfile.Coverage()
  def coverage = util.getCoverageFromJacoco(htmlPath)
  util.postCoverage(coverage, threshold)
}
