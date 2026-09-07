package dev.iurysouza.modulegraph

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Functional coverage for showTransitiveDependencies limiting graph depth (issue #69).
 */
@Suppress("LongMethod")
class ShowTransitiveDependenciesFunctionalTest {
    @TempDir
    lateinit var testProjectDir: File
    private lateinit var settingsFile: File
    private lateinit var readmeFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        readmeFile = File(testProjectDir, "README.md")
    }

    @Test
    fun `showTransitiveDependencies false shows only direct dependencies of root modules`() {
        settingsFile.writeText(
            """
                plugins { id("$MODULEGRAPH_PACKAGE.settings") }
                rootProject.name = "test"
                include(":app")
                include(":feature")
                include(":core")
            """.trimIndent(),
        )
        File(testProjectDir, "build.gradle.kts").writeText(
            """
                moduleGraphConfig {
                    heading.set("### Dependency Diagram")
                    readmePath.set("${readmeFilePath()}")
                    rootModulesRegex.set(".*:app$")
                    showTransitiveDependencies.set(false)
                    showFullPath.set(true)
                }
            """.trimIndent(),
        )
        writeJavaModule("app", listOf(":feature"))
        writeJavaModule("feature", listOf(":core"))
        writeJavaModule("core", emptyList())
        readmeFile.writeText("### Dependency Diagram")

        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("createModuleGraph")
            .withPluginClasspath()
            .build()

        val content = readmeFile.readText()
        assertTrue(content.contains(":app --> :feature"), content)
        assertFalse(content.contains(":feature --> :core"), content)
    }

    @Test
    fun `showTransitiveDependencies true includes transitive dependency edges`() {
        settingsFile.writeText(
            """
                plugins { id("$MODULEGRAPH_PACKAGE.settings") }
                rootProject.name = "test"
                include(":app")
                include(":feature")
                include(":core")
            """.trimIndent(),
        )
        File(testProjectDir, "build.gradle.kts").writeText(
            """
                moduleGraphConfig {
                    heading.set("### Dependency Diagram")
                    readmePath.set("${readmeFilePath()}")
                    rootModulesRegex.set(".*:app$")
                    showTransitiveDependencies.set(true)
                    showFullPath.set(true)
                }
            """.trimIndent(),
        )
        writeJavaModule("app", listOf(":feature"))
        writeJavaModule("feature", listOf(":core"))
        writeJavaModule("core", emptyList())
        readmeFile.writeText("### Dependency Diagram")

        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("createModuleGraph")
            .withPluginClasspath()
            .build()

        val content = readmeFile.readText()
        assertTrue(content.contains(":app --> :feature"), content)
        assertTrue(content.contains(":feature --> :core"), content)
    }

    private fun writeJavaModule(path: String, projectDeps: List<String>) {
        val deps = projectDeps.joinToString("\n") {
            "    implementation(project(\"$it\"))"
        }
        File(testProjectDir, "$path/build.gradle.kts").apply {
            parentFile.mkdirs()
            writeText(
                """
                    plugins { java }
                    dependencies {
                $deps
                    }
                """.trimIndent(),
            )
        }
    }

    /**
     * This is for Windows compatibility, as the path is used in the build file
     */
    private fun readmeFilePath() = readmeFile.absolutePath.replace("\\", "\\\\")
}
