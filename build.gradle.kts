import com.lightningkite.deployhelpers.*

buildscript {
    dependencies {
        classpath(libs.deployHelpers)
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    id("signing")
    `maven-publish`
}

group = "com.lightningkite"
version = "1.0-SNAPSHOT"

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
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

standardPublishing {
    name.set("KotlinX Serialization CSV Durable")
    description.set("A tool for communication between a server using LightningServer and a client.")
    github("lightningkite", "kotlinx-serialization-csv")

    licenses {
        mit()
    }

    developers {
        developer(
            id = "LightningKiteJoseph",
            name = "Joseph Ivie",
            email = "joseph@lightningkite.com",
        )
        developer(
            id = "bjsvedin",
            name = "Brady Svedin",
            email = "brady@lightningkite.com",
        )
    }
}
