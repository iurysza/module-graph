import java.util.Properties

val increaseVersion by tasks.registering {
    group = "version bumper"
    doLast {
        // check if we are on 'main' branch
        val currentBranch = "git rev-parse --abbrev-ref HEAD".runCommand()
        if (currentBranch.trim() != "main") {
            throw IllegalStateException("Task increaseVersion must be run on 'main' branch")
        }

        // Get version type from command line, default to "patch" if not provided
        val versionArg = project.findProperty("versionType") ?: "patch"

        val properties = Properties()
        file("./plugin-build/gradle.properties").inputStream().use { properties.load(it) }
        val oldVersion = properties.getProperty("VERSION")
        val versionParts = oldVersion.split(".")
        val majorVersion = versionParts[0].toInt()
        val minorVersion = versionParts[1].toInt()
        val patchVersion = versionParts[2].toInt()

        val newVersion = when (versionArg) {
            "major" -> "${majorVersion + 1}.0.0"
            "minor" -> "$majorVersion.${minorVersion + 1}.0"
            "patch" -> "$majorVersion.$minorVersion.${patchVersion + 1}"
            else -> throw IllegalArgumentException("Invalid version type. Valid types are: major, minor, patch")
        }
        properties.setProperty("VERSION", newVersion)
        file("./plugin-build/gradle.properties").outputStream().use { properties.store(it, null) }

        val readme = project.file("README.md").readText()
        val updatedReadme = readme.replace(Regex("""\d+\.\d+\.\d+"""), newVersion)
        project.file("README.md").writeText(updatedReadme)

        println("New version: $newVersion")

        // Stage changes, commit, push, and tag
        "git add ./plugin-build/gradle.properties ./README.md".runCommand()
        "git commit -m 'Bump version to $newVersion'".runCommand()
        "git push origin main".runCommand()
        "git tag -a v$newVersion -m 'v$newVersion'".runCommand()
        "git push origin v$newVersion".runCommand()
    }
}

fun String.runCommand(): String {
    return ProcessBuilder(*split(" ").toTypedArray())
        .directory(project.rootDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start().let {
            it.waitFor()
            it.inputStream.bufferedReader().readText()
        }
}
