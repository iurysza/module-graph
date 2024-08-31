#!/usr/bin/env kotlin

import java.io.File
import java.io.IOException
import java.util.Properties
import kotlin.system.exitProcess

val VERSION_FILE = "./plugin-build/gradle.properties"
val README_FILE = "./README.md"

enum class VersionPart { MAJOR, MINOR, PATCH }

fun bumpVersion(part: VersionPart, release: Boolean) {
    val currentVersion = getCurrentVersion()
    val newVersion = incrementVersion(currentVersion, part)
    updateVersionFile(newVersion)
    updateReadmeVersions(newVersion)
    commitTagAndPush(newVersion)

    if (release) {
        createGithubRelease(newVersion)
        waitAndViewGithubAction()
    }
}

fun getCurrentVersion(): String {
    return try {
        val properties = Properties()
        File(VERSION_FILE).inputStream().use { properties.load(it) }
        properties.getProperty("VERSION") ?: throw IOException("Version not found in properties file")
    } catch (e: IOException) {
        println("Version file not found!")
        exitProcess(1)
    }
}

fun incrementVersion(currentVersion: String, part: VersionPart): String {
    val (major, minor, patch) = currentVersion.split(".").map { it.toInt() }

    return when (part) {
        VersionPart.MAJOR -> "v${major + 1}.0.0"
        VersionPart.MINOR -> "v$major.${minor + 1}.0"
        VersionPart.PATCH -> "v$major.$minor.${patch + 1}"
    }
}

fun updateVersionFile(newVersion: String) {
    val properties = Properties()
    File(VERSION_FILE).inputStream().use { properties.load(it) }
    properties.setProperty("VERSION", newVersion.removePrefix("v"))
    File(VERSION_FILE).outputStream().use { properties.store(it, null) }
    println(properties)
}

fun updateReadmeVersions(newVersion: String) {
    try {
        val readmeContent = File(README_FILE).readText()
        val updatedContent = readmeContent.replace(
            Regex("""dev\.iurysouza:modulegraph:\d+\.\d+\.\d+"""),
            "dev.iurysouza:modulegraph:${newVersion.removePrefix("v")}"
        )
        File(README_FILE).writeText(updatedContent)
        println("Updated version references in README to $newVersion")
    } catch (e: IOException) {
        println("An error occurred while updating README: ${e.message}")
        exitProcess(1)
    }
}

fun commitTagAndPush(newVersion: String) {
    try {
        println("Committing changes...")
        runCommand("git", "add", VERSION_FILE)
        runCommand("git", "commit", "-m", "Bump version to $newVersion")
        runCommand("git", "push")
        runCommand("git", "tag", newVersion)
        runCommand("git", "push", "origin", newVersion)
        println("Version updated to $newVersion and tag pushed")
    } catch (e: IOException) {
        println("An error occurred while running git commands: ${e.message}")
        exitProcess(1)
    }
}

fun createGithubRelease(newVersion: String) {
    try {
        println("Creating GitHub release...")
        runCommand("gh", "release", "create", newVersion, "--title", newVersion, "--notes", "Auto release")
        println("GitHub release for $newVersion created successfully")
    } catch (e: IOException) {
        println("An error occurred while creating GitHub release: ${e.message}")
        exitProcess(1)
    }
}

fun waitAndViewGithubAction() {
    Thread.sleep(1000)
    runCommand("gh", "run", "view", "-w")
}

fun runCommand(vararg command: String) {
    val process = ProcessBuilder(command.toList())
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        throw IOException("Command '${command.joinToString(" ")}' failed with exit code $exitCode")
    }
}

val part = when (args.getOrNull(0)?.uppercase()) {
    "MAJOR" -> VersionPart.MAJOR
    "MINOR" -> VersionPart.MINOR
    "PATCH" -> VersionPart.PATCH
    else -> VersionPart.PATCH
}
val release = args.getOrNull(1)?.toBoolean() ?: false

println("Bumping version...")
println("Part: $part")
println("Release: $release")

try {
    bumpVersion(part, release)
} catch (e: Exception) {
    println("Error: ${e.message}")
    exitProcess(1)
}
