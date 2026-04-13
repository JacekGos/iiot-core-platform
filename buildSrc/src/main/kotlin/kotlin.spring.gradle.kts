package buildsrc.convention

plugins {
    id("buildsrc.convention.kotlin.jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    add("implementation", "org.springframework.boot:spring-boot-starter")
    add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
}