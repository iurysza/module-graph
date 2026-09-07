package dev.iurysouza.modulegraph

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * Regression coverage for Gradle overlapping-output validation
 * when createModuleGraph runs alongside other tasks (issue #72).
 */
@Suppress("LongMethod")
class CreateModuleGraphOverlappingOutputsFunctionalTest {
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
    fun `createModuleGraph can run alongside other tasks without overlapping output validation`() {
        settingsFile.writeText(
            """
                rootProject.name = "test"
                include(":example")
                include(":groupFolder:example2")
            """.trimIndent(),
        )

        exampleBuildFile.writeText(
            """
                plugins {
                    java
                    id("$MODULEGRAPH_PACKAGE")
                }

                moduleGraphConfig {
                    heading.set("### Dependency Diagram")
                    readmePath.set("${readmeFilePath()}")
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
        readmeFile.writeText("### Dependency Diagram")

        // Running createModuleGraph with another task that writes under build/
        // used to fail Gradle overlapping-output validation when projectDirectory
        // was wrongly declared as @OutputDirectory (issue #72).
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("test", "createModuleGraph", "--stacktrace")
            .withPluginClasspath()
            .build()

        assertTrue(result.output.contains("BUILD SUCCESSFUL") || result.tasks.isNotEmpty())
        assertTrue(readmeFile.readText().contains(":example --> :groupFolder:example2"))
        assertFalse(
            result.output.contains("implicit dependency") ||
                result.output.contains("overlapping output"),
            result.output,
        )
    }

    /**
     * This is for Windows compatibility, as the path is used in the build file
     */
    private fun readmeFilePath() = readmeFile.absolutePath.replace("\\", "\\\\")
}
