plugins {
    id("mnemos.spring-service")
    id("mnemos.spring-integration-test")
}

dependencies {
    implementation(project(":libraries:common-core"))
    implementation(project(":libraries:common-web"))
    implementation(project(":libraries:security-starter"))
    implementation(project(":libraries:inbox-starter"))
    implementation(project(":libraries:kafka-starter"))

    implementation(project(":contracts:account-contracts"))
    implementation(project(":contracts:social-contracts"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(libs.uuid.creator)

    implementation("org.flywaydb:flyway-database-postgresql:12.4.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.testcontainers:testcontainers-postgresql")
}

tasks.bootJar {
    archiveFileName.set("profile-service.jar")
}
