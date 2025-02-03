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
) {
    val fullyPushed get() = workingTreeClean && ahead == 0 && behind == 0
}
internal fun File.getGitStatus(): GitStatus = runCli("git", "status").let {
    GitStatus(
        branch = it.substringAfter("On branch ", "").substringBefore('\n').trim(),
        workingTreeClean = it.contains("working tree clean", ignoreCase = true),
        ahead = it.substringAfter("Your branch is ahead", "")
            .substringAfter('\'')
            .substringAfter('\'')
            .substringAfter("by ")
            .substringBefore(" commits")
            .toIntOrNull() ?:
            it.substringBefore(" different commits each, respectively", "")
                .substringAfter("and have ")
                .substringAfter(" and ")
                .toIntOrNull() ?: 0,
        behind = it.substringAfter("Your branch is behind", "")
            .substringAfter('\'')
            .substringAfter('\'')
            .substringAfter("by ")
            .substringBefore(" commits")
            .toIntOrNull() ?:
        it.substringBefore(" different commits each, respectively", "")
            .substringAfter("and have ")
            .substringBefore(" and ")
            .toIntOrNull() ?: 0,
    )
}
internal fun File.isGitClean(): Boolean = runCli("git", "status").contains("working tree clean", true)
internal fun File.isGitAhead(): Boolean = runCli("git", "status").contains("Your branch is ahead", true)
internal fun File.isGitBehind(): Boolean = runCli("git", "status").contains("Your branch is behind", true)
internal val myPatchNumberFile = rootDir.resolve("versioning/patchVersionNumber.txt").also { it.parentFile!!.mkdirs() }
internal data class PatchData(val gitHash: String, val number: Int)
internal fun patchNumber(): PatchData? {
    val current = myPatchNumberFile.takeIf { it.exists() }?.readText()?.let {
        PatchData(it.substringBefore('\n').trim(), it.substringAfter('\n').trim().toInt())
    } ?: PatchData("no commits", 0)
    if(!rootDir.getGitStatus().fullyPushed) return null
    val hash = rootDir.getGitHash()
    return if(hash == current.gitHash) current
    else {
        val new = PatchData(hash, current.number + 1)
        myPatchNumberFile.writeText(new.gitHash + "\n" + new.number)
        new
    }
}
internal fun gitCreateVersionTag() {
    val v = gitBasedVersion()
    if(v.endsWith("snapshot")) throw IllegalStateException()
    rootDir.runCli("git", "tag", v)
}

internal fun File.getGitTag(): String? =
    runCli("git", "tag", "--points-at", getGitHash()).trim().takeUnless { it.isBlank() }

val minorVersion: String? by project
val useBranchSnapshotWhenPublishing: String? by project
fun gitBasedVersion(): String {
    return if (useBranchSnapshotWhenPublishing?.toBoolean() == true)
        project.rootDir.getGitBranch() + "-SNAPSHOT"
    else {
        val versionMajor = project.rootDir.getGitBranch().let {
            if(it.startsWith("version-")) it.removePrefix("version-").toIntOrNull() ?: "0"
            else "0"
        }
        val versionMinor = minorVersion ?: "0"
        val versionPatch = if(project.rootDir.getGitStatus().fullyPushed) patchNumber()?.number
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
