plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    val kotlinVersion = libs.versions.kotlin.get()
    val springBootVersion = libs.versions.spring.boot.get()
    val springDependencyManagementVersion =
        libs.versions.spring.dependency.management.get()

    implementation(
        "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    )

    implementation(
        "org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion"
    )

    implementation(
        "org.jetbrains.kotlin:kotlin-noarg:$kotlinVersion"
    )

    implementation(
        "org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion"
    )

    implementation(
        "io.spring.gradle:dependency-management-plugin:$springDependencyManagementVersion"
    )
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}