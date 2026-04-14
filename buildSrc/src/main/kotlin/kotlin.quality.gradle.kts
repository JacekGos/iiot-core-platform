package buildsrc.convention

plugins {
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

ktlint {
    version.set("1.3.1")
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    filter {
        exclude("**/generated/**")
        include("**/*.kt")
    }
}

detekt {
    baseline = file("$rootDir/detekt-baseline.xml")
    config.setFrom(files("$rootDir/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
}

tasks.named("check") {
    dependsOn("ktlintCheck", "detekt")
}