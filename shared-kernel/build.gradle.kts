plugins {
    id("buildsrc.convention.kotlin.jvm")
    id("buildsrc.convention.kotlin.quality")
    alias(libs.plugins.kotlinPluginSerialization)
}

// shared-kernel is pure Kotlin — no Spring dependencies allowed here.
dependencies {
    api(libs.kotlinxSerialization)

    testImplementation(kotlin("test-junit5"))
}
