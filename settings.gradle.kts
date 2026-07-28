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
    ":libraries:messaging-core",
    ":libraries:kafka-starter",
    ":libraries:outbox-starter",

    ":contracts:account-events",

    ":keycloak-extensions:telegram-authenticator",

    ":apps:account-service",
    ":apps:wallet-service",
)