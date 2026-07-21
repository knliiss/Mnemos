plugins {
    base
}

group = "com.mnemos"
version = "1.0.0"

tasks.wrapper {
    gradleVersion = "9.2.1"
    distributionType = Wrapper.DistributionType.BIN
}