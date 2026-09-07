package dev.iurysouza.modulegraph

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Regression coverage for graph-only configs that must not write
 * the primary README Module Graph section (issue #70).
 */
@Suppress("LongMethod")
class GraphOnlyConfigFunctionalTest {
    @TempDir
    lateinit var testProjectDir: File
    private lateinit var settingsFile: File
    private lateinit var exampleBuildFile: File
    private lateinit var example2BuildFile: File
    private lateinit var readmeFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        exampleBuildFile = File(testProjectDir, "example/build.gradle.kts")
        example2BuildFile = File(testProjectDir, "groupFolder/example2/build.gradle.kts")
        exampleBuildFile.parentFile.mkdirs()
        example2BuildFile.parentFile.mkdirs()
        readmeFile = File(testProjectDir, "README.md")
    }

    @Test
    fun `graph-only config does not write primary README Module Graph section`() {
        settingsFile.writeText(
            """
                rootProject.name = "test"
                include(":example")
                include(":groupFolder:example2")
            """.trimIndent(),
        )

        val customReadme = File(testProjectDir, "docs/MODULE_GRAPHS.md")
        customReadme.parentFile.mkdirs()
        customReadme.writeText("## My Custom Graph")
        val customPath = customReadme.absolutePath.replace("\\", "\\\\")
        val rootReadmeOriginal = "# Untouched Root Readme\n"
        readmeFile.writeText(rootReadmeOriginal)

        exampleBuildFile.writeText(
            """
                plugins {
                    java
                    id("$MODULEGRAPH_PACKAGE")
                }

                moduleGraphConfig {
                    graph(
                        readmePath = "$customPath",
                        heading = "## My Custom Graph",
                    ) {
                        orientation = $MODULEGRAPH_PACKAGE.Orientation.LEFT_TO_RIGHT
                    }
                }
                dependencies {
                    implementation(project(":groupFolder:example2"))
                }
            """.trimIndent(),
        )
        example2BuildFile.writeText(
            """
                plugins {
                    java
                }
            """.trimIndent(),
        )

        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("createModuleGraph")
            .withPluginClasspath()
            .build()

        assertEquals(rootReadmeOriginal, readmeFile.readText())
        assertTrue(customReadme.readText().contains(":example --> :groupFolder:example2"))
        assertFalse(readmeFile.readText().contains("# Module Graph"))
    }
}
