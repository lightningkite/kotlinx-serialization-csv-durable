import com.lightningkite.deployhelpers.*

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    id("signing")
    alias(libs.plugins.vannitechPublishing)
    alias(libs.plugins.dokka)
    `maven-publish`
}

buildscript {
    repositories {
        mavenLocal()
        maven("https://lightningkite-maven.s3.us-west-2.amazonaws.com")
    }
    dependencies {
        classpath("com.lightningkite:lk-gradle-helpers:3.0.1")
    }
}

group = "com.lightningkite"
useGitBasedVersion()
useLocalDependencies()
publishing()
setupDokka("lightningkite", "kotlinx-serialization-csv-durable")

kotlin {
    jvmToolchain(17)
    explicitApi()
    applyDefaultHierarchyTemplate()
    jvm {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
    js(IR) {
        browser()
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
        val commonMain by getting {
            dependencies {
                api(libs.kotlinXJson)
                api(libs.serializationProperties)

                implementation(libs.kotlinStdLib)

            }
            kotlin {
                srcDir(file("build/generated/ksp/common/commonMain/kotlin"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
            kotlin {
                srcDir(file("build/generated/ksp/common/commonTest/kotlin"))
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    pom {
        name.set("KotlinX Serialization CSV Durable")
        description.set("A format for KotlinX Serialization that handles CSV files with a header row.")
        github("lightningkite", "kotlinx-serialization-csv-durable")
        url.set(dokkaPublicHostingIndex)
        licenses { mit() }
        developers {
            joseph()
            brady()
        }
    }
}

