plugins {
    id("org.jetbrains.kotlin.jvm")
    id("io.spring.dependency-management")
}

extra["tomcat.version"] = "11.0.24"

dependencyManagement {
    imports {
        mavenBom(
            "org.springframework.boot:spring-boot-dependencies:4.1.1",
        )
    }
}

dependencies {
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
