pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

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
    ":libraries:inbox-starter",

    ":contracts:account-contracts",
    ":contracts:wallet-contracts",
    ":contracts:event-contracts",
    ":contracts:moderation-contracts",
    ":contracts:bet-contracts",
    ":contracts:realtime-contracts",
    ":contracts:personalization-contracts",
    ":contracts:social-contracts",
    ":contracts:player-directory-contracts",
    ":contracts:payment-contracts",
    ":contracts:telegram-bot-contracts",

    ":keycloak-extensions:telegram-authenticator",

    ":apps:account-service",
    ":apps:wallet-service",
    ":apps:event-service",
    ":apps:notification-service",
    ":apps:reward-service",
    ":apps:bet-service",
    ":apps:moderation-service",
    ":apps:gateway-service",
    ":apps:realtime-service",
    ":apps:personalization-service",
    ":apps:social-service",
    ":apps:profile-service",
    ":apps:player-directory-service",
    ":apps:payment-service",
    ":apps:telegram-bot-service",
)
