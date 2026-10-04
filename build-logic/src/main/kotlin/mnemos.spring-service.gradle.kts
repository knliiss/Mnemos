import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import org.gradle.language.base.plugins.LifecycleBasePlugin

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.jetbrains.kotlin.plugin.jpa")
    id("org.jlleitschuh.gradle.ktlint")

    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

extra["tomcat.version"] = "11.0.25"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("mnemos.kafka.message-auth.enabled", "false")
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("integration")
    }
}

val testSourceSet = extensions.getByType<SourceSetContainer>().named("test")
val hasIntegrationTaggedTests =
    fileTree("src/test") {
        include("**/*.kt", "**/*.java")
    }.files.any { source ->
        source.readText().contains("@Tag(\"integration\")")
    }

val integrationTest =
    tasks.register<Test>("integrationTest") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Runs integration-tagged tests."

        testClassesDirs = testSourceSet.get().output.classesDirs
        classpath = testSourceSet.get().runtimeClasspath

        shouldRunAfter(tasks.named("test"))

        useJUnitPlatform {
            includeTags("integration")
        }

        enabled = hasIntegrationTaggedTests
    }

tasks.named("check") {
    dependsOn(integrationTest)
}

tasks.register("fastCheck") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs source formatting and non-integration tests for fast CI."

    dependsOn(
        tasks.named("ktlintCheck"),
        tasks.named("test"),
    )
}
