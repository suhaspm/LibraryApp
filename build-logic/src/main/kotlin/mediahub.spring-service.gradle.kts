// Convention plugin shared by every Spring Boot service: one place to change the
// Java version, test setup and container-friendly jar layout for all services.
plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "io.mediahub"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    "implementation"("org.springframework.boot:spring-boot-starter-actuator")
    "implementation"("org.springframework.boot:spring-boot-starter-validation")
    "runtimeOnly"("io.micrometer:micrometer-registry-prometheus")

    "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    "testImplementation"("org.springframework.boot:spring-boot-testcontainers")
    "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// A plain jar next to the boot jar only confuses the Dockerfile's COPY.
tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName = "app.jar"
}
