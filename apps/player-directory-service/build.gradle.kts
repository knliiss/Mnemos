plugins {
    id("mnemos.spring-service")
    id("mnemos.spring-integration-test")
}

val springCloudVersion by extra("2025.1.2")

dependencies {
    implementation(project(":libraries:common-core"))
    implementation(project(":libraries:common-web"))
    implementation(project(":libraries:security-starter"))
    implementation(project(":libraries:inbox-starter"))
    implementation(project(":libraries:outbox-starter"))
    implementation(project(":libraries:kafka-starter"))

    implementation(project(":contracts:account-contracts"))
    implementation(project(":contracts:event-contracts"))
    implementation(project(":contracts:player-directory-contracts"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("io.github.openfeign:feign-okhttp")
    implementation("org.flywaydb:flyway-database-postgresql:13.10.3")
    implementation(libs.uuid.creator)

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
    archiveFileName.set("player-directory-service.jar")
}
