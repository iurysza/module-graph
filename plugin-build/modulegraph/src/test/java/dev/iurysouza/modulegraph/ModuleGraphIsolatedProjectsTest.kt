package dev.iurysouza.modulegraph

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/** Verifies the graph is generated with the Configuration Cache and Isolated Projects enabled. */
class ModuleGraphIsolatedProjectsTest {
    @TempDir
    lateinit var testProjectDir: File
    private lateinit var settingsFile: File
    private lateinit var rootBuildFile: File
    private lateinit var readmeFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        rootBuildFile = File(testProjectDir, "build.gradle.kts")
        readmeFile = File(testProjectDir, "README.md")
    }

    @Test
    fun `graph is generated with isolated projects enabled`() {
        settingsFile.writeText(
            """
                plugins { id("$MODULEGRAPH_PACKAGE.settings") }
                rootProject.name = "ip-sample"
                include(":app")
                include(":core:data")
                include(":core:ui")
            """.trimIndent(),
        )
        rootBuildFile.writeText(
            """
                moduleGraphConfig {
                    heading.set("### Module Graph")
                    readmePath.set("${readmeFilePath()}")
                }
            """.trimIndent(),
        )
        writeModule("app", listOf(":core:data", ":core:ui"))
        writeModule("core/data", emptyList())
        writeModule("core/ui", listOf(":core:data"))
        readmeFile.writeText("### Module Graph")

        val result = GradleRunner.create().withProjectDir(testProjectDir).withArguments(
            "createModuleGraph",
            "--configuration-cache",
            "-Dorg.gradle.unsafe.isolated-projects=true",
        ).withPluginClasspath().build()

        val output = result.output
        assertFalse(
            output.contains("problems were found storing the configuration cache"),
            output,
        )
        assertTrue(
            readmeFile.readText().contains(":app --> :core:data"),
            readmeFile.readText(),
        )
        assertTrue(
            readmeFile.readText().contains(":app --> :core:ui"),
            readmeFile.readText(),
        )
        assertTrue(
            readmeFile.readText().contains(":core:ui --> :core:data"),
            readmeFile.readText(),
        )

        // Second run reuses the configuration cache.
        val rerun = GradleRunner.create().withProjectDir(testProjectDir).withArguments(
            "createModuleGraph",
            "--configuration-cache",
            "-Dorg.gradle.unsafe.isolated-projects=true",
        ).withPluginClasspath().build()
        assertTrue(
            rerun.output.contains("Reusing configuration cache"),
            rerun.output,
        )
    }

    private fun writeModule(path: String, projectDeps: List<String>) {
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

    private fun readmeFilePath() = readmeFile.absolutePath.replace(
        "\\",
        "\\\\",
    )
}
