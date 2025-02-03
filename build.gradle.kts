import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.internal.impldep.org.joda.time.format.DateTimeFormat
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    id("signing")
    id("com.vanniktech.maven.publish") version "0.30.0"
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

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}

publishing {
    repositories {
        val lightningKiteMavenAwsAccessKey: String? by project
        val lightningKiteMavenAwsSecretAccessKey: String? by project
        lightningKiteMavenAwsAccessKey?.let { ak ->
            maven {
                name = "LightningKite"
                url = uri("s3://lightningkite-maven")
                credentials(AwsCredentials::class) {
                    accessKey = ak
                    secretKey = lightningKiteMavenAwsSecretAccessKey!!
                }
            }
        }
    }
}


internal fun File.runCli(vararg args: String): String {
    val process = ProcessBuilder(*args)
        .directory(this)
        .start()
    process.outputStream.close()
    return process.inputStream.readAllBytes().toString(Charsets.UTF_8)
}

internal fun File.getGitCommitTime(): OffsetDateTime = OffsetDateTime.parse(runCli("git", "show", "--no-patch", "--format=%ci", "HEAD").trim())
internal fun File.getGitBranch(): String = runCli("git", "rev-parse", "--abbrev-ref", "HEAD").trim()
internal fun File.getGitHash(): String = runCli("git", "rev-parse", "--short", "HEAD").trim()

internal data class GitStatus(
    val branch: String,
    val workingTreeClean: Boolean,
    val ahead: Int,
    val behind: Int,
)
//internal fun File.getGitStatus(): Boolean = runCli("git", "status").let {}
internal fun File.isGitClean(): Boolean = runCli("git", "status").contains("working tree clean", true)
internal fun File.isGitAhead(): Boolean = runCli("git", "status").contains("Your branch is ahead", true)
internal fun File.isGitBehind(): Boolean = runCli("git", "status").contains("Your branch is behind", true)

internal fun File.getGitTag(): String? =
    runCli("git", "tag", "--points-at", getGitHash()).trim().takeUnless { it.isBlank() }

val useBranchSnapshotWhenPublishing: String? by project
fun gitBasedVersion(): String {
    return if (useBranchSnapshotWhenPublishing?.toBoolean() == true)
        project.rootDir.getGitBranch() + "-SNAPSHOT"
    else {
        val versionMajor = project.rootDir.getGitBranch().let {
            if(it.startsWith("version-")) it.removePrefix("version-").toIntOrNull() ?: "0"
            else "0"
        }
        val versionMinor = "1"
        val versionPatch = if(project.rootDir.isGitClean()) project.rootDir.getGitCommitTime().withOffsetSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"))
        else null
        "${versionMajor}.${versionMinor}" + (versionPatch?.let { ".$it" } ?: "-SNAPSHOT")
    }
}

val lockFile = project.projectDir.resolve("mavenOrLocal.lock")
val branchModeProjectsFolder: String? by project
fun mavenOrLocal(group: String, artifact: String, major: Int, minor: Int): Any {
    return branchModeProjectsFolder?.let {
        "$group:$artifact:version-$major-SNAPSHOT"
    } ?: "$group:$artifact:$major.$minor.+"
}

println("Proposed version: ${gitBasedVersion()}")

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates(group.toString(), name, version.toString())
    pom {
        name.set("KotlinX Serialization CSV Durable")
        description.set("A tool for communication between a server using LightningServer and a client.")
        url.set("https://github.com/lightningkite/kotlinx-serialization-csv-durable")
        scm {
            url.set("https://github.com/lightningkite/kotlinx-serialization-csv-durable")
            connection.set("https://github.com/lightningkite/kotlinx-serialization-csv-durable.git")
            developerConnection.set("https://github.com/lightningkite/kotlinx-serialization-csv-durable.git")
        }

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit")
                distribution.set("https://opensource.org/license/mit")
            }
        }

        developers {
            developer {
                id.set("LightningKiteJoseph")
                name.set("Joseph Ivie")
                email.set("joseph@lightningkite.com")
            }
            developer {
                id.set("bjsvedin")
                name.set("Brady Svedin")
                email.set("brady@lightningkite.com")
            }
        }
    }
}
