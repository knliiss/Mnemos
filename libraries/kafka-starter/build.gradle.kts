plugins {
    id("mnemos.spring-library")
}

dependencies {
    api(project(":libraries:messaging-core"))

    compileOnly(project(":libraries:inbox-starter"))
    testImplementation(project(":libraries:inbox-starter"))

    api(libs.spring.boot.starter.kafka)
    implementation(libs.spring.boot.autoconfigure)
    api(libs.jackson.kotlin)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.kotlin)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
