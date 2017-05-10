def call(Map args) {
  def threshold = args.threshold ?: 80
  def xmlPath = args.xmlPath ?: "target/site/jacoco/jacoco.xml"

  def util = new com.spotify.jenkinsfile.Coverage()
  def coverage = util.getCoverageFromJacoco(xmlPath)
  util.postCoverage(coverage, threshold)
}
