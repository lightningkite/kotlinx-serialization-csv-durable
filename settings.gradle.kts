
pluginManagement {
    repositories {
        mavenLocal()
        google()
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/releases/")
    }

    plugins {
    }

    dependencyResolutionManagement {
        repositories {
            mavenLocal()
            google()
            gradlePluginPortal()
            mavenCentral()
            maven("https://jitpack.io")
        }
    }
}

rootProject.name = "kotlinx-serialization-csv-durable"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}