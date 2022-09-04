import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    kotlin("jvm") version "1.7.10"
    id("com.vanniktech.maven.publish") version "0.19.0"
    id("com.diffplug.gradle.spotless") version "3.27.0"
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    id("org.jetbrains.dokka") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

//apply(plugin = "com.vanniktech.maven.publish")

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("org.http4k:http4k-core:4.30.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation(kotlin("test"))
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        target(fileTree(rootDir) { include("**/*.kt") })

        // by default the target is every '.kt' and '.kts` file in the java sourcesets
        ktlint("0.43.2").userData(mapOf("indent_size" to "2", "continuation_indent_size" to "2"))
        licenseHeaderFile(file("$rootDir/src/spotless/spotless.kotlin.license"))
    }
    kotlinGradle {
        target("*.gradle.kts") // default target for kotlinGradle
        ktlint("0.43.2")
        endWithNewline()
        trimTrailingWhitespace()
    }
    encoding("UTF-8")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "1.8"
}
tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "1.8"
}

