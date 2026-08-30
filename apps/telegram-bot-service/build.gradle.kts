plugins {
    id("mnemos.spring-service")
    id("mnemos.spring-integration-test")
}

val springCloudVersion by extra("2025.1.2")

dependencies {
    implementation(project(":libraries:common-core"))
    implementation(project(":libraries:common-web"))
    implementation(project(":libraries:security-starter"))

    implementation(project(":contracts:account-contracts"))
    implementation(project(":contracts:event-contracts"))
    implementation(project(":contracts:payment-contracts"))
    implementation(project(":contracts:player-directory-contracts"))
    implementation(project(":contracts:telegram-bot-contracts"))
    implementation(project(":contracts:wallet-contracts"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation(libs.telegram.bot)
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

tasks.bootJar {
    archiveFileName.set("telegram-bot-service.jar")
}
