plugins {
    id("mediahub.spring-service")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation(libs.springdoc.webmvc.ui)

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-mongodb")
}
