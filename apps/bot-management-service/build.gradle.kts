plugins {
    id("mnemos.spring-service")
}

dependencies {
    implementation(project(":libraries:common-core"))
    implementation(project(":libraries:common-web"))
    implementation(project(":libraries:security-starter"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.postgresql:postgresql")
}

tasks.bootJar {
    archiveFileName.set("bot-management-service.jar")
}
