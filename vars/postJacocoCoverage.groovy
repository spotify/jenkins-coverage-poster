def call(Map args) {
  final Double threshold = args.threshold ?: 80.0
  final String xmlPath = args.xmlPath ?: "target/site/jacoco/jacoco.xml"

  final util = new com.spotify.jenkinsfile.Coverage()
  final Double coverage = util.getCoverageFromJacoco(xmlPath)
  util.postCoverage(coverage, threshold)
}
