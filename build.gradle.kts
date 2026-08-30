plugins {
    base
    id("org.owasp.dependencycheck") version "13.0.0"
}

group = "com.mnemos"
version = "1.0.0"

dependencyCheck {
    failBuildOnCVSS = 7.0f
    failOnError = true
    failBuildOnUnusedSuppressionRule = true
    scanConfigurations = listOf("runtimeClasspath")
    suppressionFile = "config/dependency-check-suppressions.xml"

    analyzers.assemblyEnabled = false

    val nvdApiKey = providers.environmentVariable("NVD_API_KEY").orNull

    if (!nvdApiKey.isNullOrBlank()) {
        nvd.apiKey.set(nvdApiKey)
    } else {
        nvd.datafeedUrl.set(
            "https://dependency-check.github.io/DependencyCheck_Builder/nvd_cache/nvdcve-{0}.json.gz",
        )
    }
}

tasks.wrapper {
    gradleVersion = "9.7.1"
    distributionType = Wrapper.DistributionType.BIN
}
