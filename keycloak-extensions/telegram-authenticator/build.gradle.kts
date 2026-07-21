plugins {
    id("mnemos.keycloak-extension")
}

version = "1.0.0-SNAPSHOT"

dependencies {
    compileOnly("com.fasterxml.jackson.core:jackson-databind")
}

tasks.jar {
    archiveFileName.set("mnemos-telegram-authenticator.jar")
}