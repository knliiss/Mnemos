import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType

plugins {
    kotlin("jvm")
}

val libs = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")

    testImplementation(libs.findLibrary("mockito-kotlin").get())

    mockitoAgent("org.mockito:mockito-core") {
        isTransitive = false
    }

    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}