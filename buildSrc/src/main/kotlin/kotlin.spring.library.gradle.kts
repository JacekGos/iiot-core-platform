package buildsrc.convention

plugins {
    id("buildsrc.convention.kotlin.jvm")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
    id("buildsrc.convention.kotlin.quality")
}

configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.5")
    }
}

// shared-kernel is a library, not a runnable app
// so we disable bootJar and enable plain jar
tasks.named("jar") {
    enabled = true
}



dependencies {
    add("implementation", "org.springframework.boot:spring-boot-starter")
    add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
}