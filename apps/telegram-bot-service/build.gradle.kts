plugins {
    id("mnemos.spring-service")
    id("mnemos.spring-integration-test")
}

dependencies {
    implementation(project(":libraries:common-core"))
    implementation(project(":libraries:common-web"))
    implementation(project(":libraries:security-starter"))

    implementation(project(":contracts:telegram-bot-contracts"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(libs.telegram.bot)
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.bootJar {
    archiveFileName.set("telegram-bot-service.jar")
}
