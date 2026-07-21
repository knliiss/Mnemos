pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(
        RepositoriesMode.FAIL_ON_PROJECT_REPOS
    )

    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.version.toml"))
        }
    }

}

rootProject.name = "Mnemos"

include(
    ":libraries:common-core",
    ":libraries:common-web",
    ":libraries:security-starter",
    ":apps:account-service",
    ":keycloak-extensions:telegram-authenticator",
)