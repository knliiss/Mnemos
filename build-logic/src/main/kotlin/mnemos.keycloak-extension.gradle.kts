import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    java
}

group = "com.mnemos.keycloak"

val keycloakVersion = "26.7.0"
val junitVersion = "5.12.2"

extensions.configure<JavaPluginExtension> {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    add(
        "compileOnly",
        "org.keycloak:keycloak-core:$keycloakVersion"
    )

    add(
        "compileOnly",
        "org.keycloak:keycloak-server-spi:$keycloakVersion"
    )

    add(
        "compileOnly",
        "org.keycloak:keycloak-server-spi-private:$keycloakVersion"
    )

    add(
        "compileOnly",
        "org.keycloak:keycloak-services:$keycloakVersion"
    )

    add(
        "testImplementation",
        platform("org.junit:junit-bom:$junitVersion")
    )

    add(
        "testImplementation",
        "org.junit.jupiter:junit-jupiter"
    )
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version.toString()
        )
    }
}