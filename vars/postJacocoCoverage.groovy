def call(Map args) {
  def threshold = args.threshold ?: 80
  def htmlPath = args.htmlPath ?: "target/site/jacoco/index.html"

  def util = new com.mpavlov.jenkinsfile.Coverage()
  def coverage = util.getCoverageFromJacoco(htmlPath)
  util.postCoverage(coverage, threshold)
}
