#!/usr/bin/env kotlin

import java.io.File
import java.io.IOException
import java.util.Properties
import kotlin.system.exitProcess

val versionFile = "./plugin-build/gradle.properties"
val readmeFile = "./README.md"

enum class VersionPart { MAJOR, MINOR, PATCH }

fun checkMainBranch() {
    try {
        val currentBranch = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
            .inputStream.bufferedReader().readText().trim()

        if (currentBranch != "main") {
            println("Error: Must be on main branch to bump version. Current branch: $currentBranch")
            exitProcess(1)
        }
    } catch (e: IOException) {
        println("Error checking git branch: ${e.message}")
        exitProcess(1)
    }
}

fun bumpVersion(part: VersionPart, release: Boolean, isSnapshot: Boolean) {
    checkMainBranch()
    
    val currentVersion = getCurrentVersion().removeSuffix("-SNAPSHOT")
    if (isSnapshot) {
        val snapshotVersion = "$currentVersion-SNAPSHOT"
        updateVersionFile(snapshotVersion)
        commitTagAndPush(snapshotVersion)
        return
    }

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
        File(versionFile).inputStream().use { properties.load(it) }
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
    File(versionFile).inputStream().use { properties.load(it) }
    properties.setProperty("VERSION", newVersion.removePrefix("v"))
    File(versionFile).outputStream().use { properties.store(it, null) }
    println(properties)
}

fun updateReadmeVersions(newVersion: String) {
    try {
        val readmeContent = File(readmeFile).readText()
        val updatedContent = readmeContent.replace(
            Regex("""dev\.iurysouza:modulegraph:\d+\.\d+\.\d+"""),
            "dev.iurysouza:modulegraph:${newVersion.removePrefix("v")}",
        )
        File(readmeFile).writeText(updatedContent)
        println("Updated version references in README to $newVersion")
    } catch (e: IOException) {
        println("An error occurred while updating README: ${e.message}")
        exitProcess(1)
    }
}

// Rollback functions for different failure scenarios
fun rollbackStaging(originalVersionContent: String) {
    println("Rolling back staging...")
    try {
        runCommand("git", "restore", "--staged", versionFile)
        File(versionFile).writeText(originalVersionContent)
        println("Rollback: File unstaged and restored to original content")
    } catch (e: IOException) {
        println("Warning: Could not rollback staging: ${e.message}")
    }
}

fun rollbackCommit(originalVersionContent: String) {
    println("Rolling back commit...")
    try {
        runCommand("git", "reset", "--hard", "HEAD~1")
        File(versionFile).writeText(originalVersionContent)
        println("Rollback: Reset HEAD and restored version file")
    } catch (e: IOException) {
        println("Warning: Could not rollback commit: ${e.message}")
    }
}

fun rollbackTagAndCommit(newVersion: String, originalVersionContent: String) {
    println("Rolling back local tag and commit...")
    try {
        runCommand("git", "tag", "-d", newVersion)
        runCommand("git", "reset", "--hard", "HEAD~1")
        File(versionFile).writeText(originalVersionContent)
        println("Rollback: Deleted local tag, reset HEAD, and restored version file")
    } catch (e: IOException) {
        println("Warning: Could not rollback tag and commit: ${e.message}")
    }
}

fun rollbackCommitOnRemote(newVersion: String, originalVersionContent: String) {
    println("Rolling back after remote commit push...")
    try {
        runCommand("git", "reset", "--hard", "HEAD~1")
        File(versionFile).writeText(originalVersionContent)
        println("WARNING: Commit was pushed to remote but tag creation failed.")
        println("WARNING: Manual cleanup required - remote has the commit but no tag.")
        println("Rollback: Reset local HEAD and restored version file")
    } catch (e: IOException) {
        println("Warning: Could not rollback local state: ${e.message}")
    }
}

fun commitTagAndPush(newVersion: String) {
    // Capture original state for rollback
    val originalVersionContent = try {
        File(versionFile).readText()
    } catch (e: IOException) {
        println("Error: Cannot read original version file for rollback")
        throw e
    }
    
    var commitCreated = false
    var commitPushed = false  
    var tagCreated = false
    
    try {
        println("Committing changes...")
        
        // Step 1: git add (low risk - no rollback needed for this step)
        runCommand("git", "add", versionFile)
        
        // Step 2: git commit 
        runCommand("git", "commit", "-m", "Bump version to $newVersion")
        commitCreated = true
        
        // Step 3: git push
        runCommand("git", "push") 
        commitPushed = true
        
        // Step 4: git tag
        runCommand("git", "tag", newVersion)
        tagCreated = true
        
        // Step 5: git push tag
        runCommand("git", "push", "origin", newVersion)
        
        println("Version updated to $newVersion and tag pushed")
        
    } catch (e: IOException) {
        println("Git operation failed: ${e.message}")
        
        // Rollback based on what operations succeeded
        when {
            tagCreated && commitPushed -> rollbackTagAndCommit(newVersion, originalVersionContent)
            commitPushed -> rollbackCommitOnRemote(newVersion, originalVersionContent)
            commitCreated -> rollbackCommit(originalVersionContent)
            else -> rollbackStaging(originalVersionContent)
        }
        
        println("Repository has been rolled back to previous state")
        throw e
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
val isSnapshot = args.getOrNull(2)?.toBoolean() ?: false

println("Bumping version...")
println("Part: $part")
println("Release: $release")

try {
    bumpVersion(part, release, isSnapshot)
} catch (e: Exception) {
    println("Error: ${e.message}")
    exitProcess(1)
}
