#!/usr/bin/env groovy

def call(Map args) {
  final Double coverageThreshold = args.threshold ?: 80.0
  final Double coverageDeltaThreshold = args.deltaThreshold ?: -1.0
  final String xmlPath = args.xmlPath ?: "target/site/jacoco/jacoco.xml"

  final util = new com.spotify.jenkinsfile.Coverage()
  final Double coverage = util.getCoverageFromJacoco(xmlPath)
  util.postCoverage(coverage, coverageThreshold)
  util.postCoverageDelta(0.0 as Double, coverageDeltaThreshold)
}
