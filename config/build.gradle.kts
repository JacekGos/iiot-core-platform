plugins {
    id("buildsrc.convention.kotlin.spring.module")
}

dependencies {
    implementation(project(":shared-kernel"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
}