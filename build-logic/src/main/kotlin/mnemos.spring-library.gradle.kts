import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import org.gradle.language.base.plugins.LifecycleBasePlugin

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("io.spring.dependency-management")
}

extra["tomcat.version"] = "11.0.25"

dependencyManagement {
    imports {
        mavenBom(
            "org.springframework.boot:spring-boot-dependencies:4.1.1",
        )
    }
}

dependencies {
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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
