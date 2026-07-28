plugins {
    id("mnemos.spring-library")
}

dependencies {
    api(project(":libraries:messaging-core"))

    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.jackson.kotlin)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.kotlin)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}