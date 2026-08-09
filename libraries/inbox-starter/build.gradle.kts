plugins {
    id("mnemos.spring-library")
}

dependencies {
    api(project(":libraries:messaging-core"))

    implementation(libs.spring.boot.starter.jdbc)
    implementation(libs.spring.boot.autoconfigure)
    
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.kotlin)

    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")

    testRuntimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}