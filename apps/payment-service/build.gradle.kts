plugins {
    id("mnemos.spring-service")
    id("mnemos.spring-integration-test")
}

val springCloudVersion by extra("2025.1.2")

dependencies {
    implementation(project(":libraries:common-core"))
    implementation(project(":libraries:common-web"))
    implementation(project(":libraries:security-starter"))

    implementation(project(":libraries:outbox-starter"))
    implementation(project(":libraries:inbox-starter"))
    implementation(project(":libraries:kafka-starter"))

    implementation(project(":contracts:payment-contracts"))
    implementation(project(":contracts:wallet-contracts"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(libs.uuid.creator)

    implementation("org.flywaydb:flyway-database-postgresql:13.3.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.testcontainers:testcontainers-postgresql")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

tasks.bootJar {
    archiveFileName.set("payment-service.jar")
}
