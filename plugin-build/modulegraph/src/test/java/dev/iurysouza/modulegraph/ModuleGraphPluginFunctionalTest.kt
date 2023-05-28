package dev.iurysouza.modulegraph

import java.io.File
import kotlin.random.Random
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@Suppress("LongMethod")
class ModuleGraphPluginFunctionalTest {
    @TempDir
    lateinit var testProjectDir: File
    private lateinit var settingsFile: File
    private lateinit var exampleBuildFile: File
    private lateinit var example2BuildFile: File
    private lateinit var example3BuildFile: File
    private lateinit var readmeFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectDir, "settings.gradle.kts")
        exampleBuildFile = File(testProjectDir, "example/build.gradle.kts")
        example2BuildFile = File(testProjectDir, "groupFolder/example2/build.gradle.kts")
        example3BuildFile = File(testProjectDir, "groupFolder/example3/build.gradle.kts")
        exampleBuildFile.parentFile.mkdirs()
        example2BuildFile.parentFile.mkdirs()
        example3BuildFile.parentFile.mkdirs()
        readmeFile = File(testProjectDir, "README.md")
    }

    @Test
    fun `when plugin is ran it produces the expected output`() {
        settingsFile.writeText(
            """
                rootProject.name = "test"
                include(":example")
                include(":groupFolder:example2")
            """.trimIndent()
        )

        exampleBuildFile.writeText(
            """
                plugins {
                    java
                    id("dev.iurysouza.modulegraph")
                }

                moduleGraphConfig {
                    heading.set("### Dependency Diagram")
                    theme.set(dev.iurysouza.modulegraph.Theme.FOREST)
                    orientation.set(dev.iurysouza.modulegraph.Orientation.RIGHT_TO_LEFT)
                    readmePath.set("${readmeFile.absolutePath.replace("\\", "\\\\")}")
                }
                dependencies {
                    implementation(project(":groupFolder:example2"))
                }
            """.trimIndent()
        )
        readmeFile.writeText("### Dependency Diagram")

        // Run the plugin task
        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("createModuleGraph")
            .withPluginClasspath()
            .build()

        // Check if the output matches the expected result
        val expectedOutput =
            """
                ### Dependency Diagram

                ```mermaid
                %%{
                  init: {
                    'theme': 'forest'
                  }
                }%%

                graph RL

                  subgraph groupFolder
                    example2
                  end
                  example --> example2
                ```
            """.trimIndent()
        assertEquals(expectedOutput, readmeFile.readText())
    }

    @Test
    fun `when custom theme is applied it produces the expected output`() {
        settingsFile.writeText(
            """
                rootProject.name = "test"
                include(":example")
                include(":groupFolder:example2")
            """.trimIndent()
        )

        exampleBuildFile.writeText(
            """
                plugins {
                    java
                    id("dev.iurysouza.modulegraph")
                }
                moduleGraphConfig {
                    heading.set("### Dependency Diagram")
                    theme.set(dev.iurysouza.modulegraph.Theme.BASE(
                        mapOf(
                            "primaryTextColor" to "#fff",
                            "primaryColor" to "#5a4f7c",
                            "primaryBorderColor" to "#5a4f7c",
                            "lineColor" to "#f5a623",
                            "tertiaryColor" to "#40375c",
                            "fontSize" to "11px"
                            )
                        )
                    )
                    readmePath.set("${readmeFile.absolutePath.replace("\\", "\\\\")}")
                }
                dependencies {
                    implementation(project(":groupFolder:example2"))
                }
            """.trimIndent()
        )
        readmeFile.writeText("### Dependency Diagram")

        // Run the plugin task
        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("createModuleGraph")
            .withPluginClasspath()
            .build()

        // Check if the output matches the expected result
        val expectedOutput =
            """
                ### Dependency Diagram

                ```mermaid
                %%{
                  init: {
                    'theme': 'base',
                	'themeVariables': {"primaryTextColor":"#fff","primaryColor":"#5a4f7c","primaryBorderColor":"#5a4f7c","lineColor":"#f5a623","tertiaryColor":"#40375c","fontSize":"11px"}
                  }
                }%%

                graph LR

                  subgraph groupFolder
                    example2
                  end
                  example --> example2
                ```
            """.trimIndent()
        assertEquals(expectedOutput, readmeFile.readText())
    }

    @Test
    fun `when showFullPath option is set to true, the generated graph should show full the path`() {
        settingsFile.writeText(
            """
                rootProject.name = "test"
                include(":example")
                include(":groupFolder:example2")
            """.trimIndent()
        )

        exampleBuildFile.writeText(
            """
                plugins {
                    java
                    id("dev.iurysouza.modulegraph")
                }
                moduleGraphConfig {
                    heading.set("### Dependency Diagram")
                    showFullPath.set(true)
                    readmePath.set("${readmeFile.absolutePath.replace("\\", "\\\\")}")
                }
                dependencies {
                    implementation(project(":groupFolder:example2"))
                }
            """.trimIndent()
        )
        readmeFile.writeText("### Dependency Diagram")

        // Run the plugin task
        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("createModuleGraph")
            .withPluginClasspath()
            .build()

        // Check if the output matches the expected result
        val expectedOutput =
            """
                ### Dependency Diagram

                ```mermaid
                %%{
                  init: {
                    'theme': 'neutral'
                  }
                }%%

                graph LR
                  :example --> :groupFolder:example2
                ```
            """.trimIndent()
        assertEquals(expectedOutput, readmeFile.readText())
    }

    @Test
    fun `plugin add configuration name to links if configured to`() {
        settingsFile.writeText(
            """
                rootProject.name = "test"
                include(":example")
                include(":groupFolder:example2")
                include(":groupFolder:example3")
            """.trimIndent()
        )

        exampleBuildFile.writeText(
            """
                plugins {
                    java
                    id("dev.iurysouza.modulegraph")
                }

                moduleGraphConfig {
                    heading.set("### Dependency Diagram")
                    theme.set(dev.iurysouza.modulegraph.Theme.FOREST)
                    orientation.set(dev.iurysouza.modulegraph.Orientation.RIGHT_TO_LEFT)
                    linkText.set(dev.iurysouza.modulegraph.LinkText.CONFIGURATION)
                    readmePath.set("${readmeFile.absolutePath.replace("\\", "\\\\")}")
                }
                dependencies {
                    implementation(project(":groupFolder:example2"))
                    runtimeOnly(project(":groupFolder:example3"))
                }
            """.trimIndent()
        )
        readmeFile.writeText("### Dependency Diagram")

        // Run the plugin task
        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("createModuleGraph")
            .withPluginClasspath()
            .build()

        // Check if the output matches the expected result
        val expectedOutput =
            """
                ### Dependency Diagram

                ```mermaid
                %%{
                  init: {
                    'theme': 'forest'
                  }
                }%%

                graph RL

                  subgraph groupFolder
                    example2
                    example3
                  end
                  example -- implementation --> example2
                  example -- runtimeOnly --> example3
                ```
            """.trimIndent()
        assertEquals(expectedOutput, readmeFile.readText())
    }

    @Test
    fun `plugin creates readme file when one is not found`() {
        val missingFile = testProjectDir.resolve("missing-file-${Random.nextLong()}.md")
        settingsFile.writeText(
            """
                rootProject.name = "test"
                include(":example")
            """.trimIndent()
        )
        exampleBuildFile.writeText(
            """
                plugins {
                    java
                    id("dev.iurysouza.modulegraph")
                }

                moduleGraphConfig {
                    heading.set("### Dependency Diagram")
                    readmePath.set("${missingFile.absolutePath.replace("\\", "\\\\")}")
                }
            """.trimIndent()
        )

        // Run the plugin task
        GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("createModuleGraph")
            .withPluginClasspath()
            .build()

        // Check that the file was created and output written
        assertTrue(missingFile.exists(), "File was supposed to be created but was not.")
        assertFalse(missingFile.readText().isBlank(), "File was created but has no content.")
    }
}
