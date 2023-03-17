package com.iurysouza.modulegraph

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ModuleGraphPluginIntegrationTest {
    @TempDir
    lateinit var testProjectDir: File
    private lateinit var settingsFile: File
    private lateinit var exampleBuildFile: File
    private lateinit var example2BuildFile: File
    private lateinit var readmeFile: File

    @BeforeEach
    fun setup() {
        settingsFile = File(testProjectDir, "settings.gradle")
        readmeFile = File(testProjectDir, "README.md")
        exampleBuildFile = File(testProjectDir, "example/build.gradle")
        example2BuildFile = File(testProjectDir, "example2/build.gradle")
        exampleBuildFile.parentFile.mkdirs()
        example2BuildFile.parentFile.mkdirs()
    }

    @Test
    fun `when plugin is ran it produces the expected output`() {
        settingsFile.writeText(
            """
                rootProject.name = 'test'
                include 'example', 'example2'
            """.trimIndent()
        )

        exampleBuildFile.writeText(
            """
                plugins {
                    id 'java'
                    id 'dev.iurysouza.modulegraph' version '0.1.1'
                }

                moduleGraphConfig {
                    heading.set("### Dependency Diagram")
                    theme.set(dev.iurysouza.modulegraph.Theme.NEUTRAL)
                    readmePath.set("${readmeFile.absolutePath}")
                }
                dependencies {
                    implementation project(':example2')
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
                  subgraph others
                    example
                    example2
                  end
                  example --> example2
                ```
            """.trimIndent()
        assertEquals(expectedOutput, readmeFile.readText())
    }
}
