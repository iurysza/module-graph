package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.*
import dev.iurysouza.modulegraph.gradle.Module
import dev.iurysouza.modulegraph.model.ProjectGraphResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MermaidGraphTest {

    @Test
    fun `Focused modules adds custom styling to mermaid code`() {
        val reconstructedModel = mapOf(
            Module(":example") to listOf(
                Module(path = ":groupFolder:example2", configName = "implementation"),
                Module(path = ":groupFolder:example3", configName = "implementation"),
            ),
        )
        val focusColor = "#F5A622"
        val config = getConfig(
            focusedModulesRegex = ".*example2.*",
            theme = Theme.BASE(focusColor = focusColor),
            showFullPath = true,
        )
        val result = ProjectGraphResult(reconstructedModel, config)
        val mermaidGraph = Mermaid.generateGraph(result)

        val expectedGraph = """
            ```mermaid
            %%{
              init: {
                'theme': 'base'
              }
            }%%

            graph LR
              :example --> :groupFolder:example2

            classDef focus fill:$focusColor,stroke:#fff,stroke-width:2px,color:#fff;
            class :groupFolder:example2 focus
            ```
        """.trimIndent()
        assertEquals(expectedGraph, mermaidGraph)
    }

    @Test
    fun `Given a single module project, when generating graph, then it throws IllegalArgumentException`() {
        val graphModel = mapOf(Module(":example") to listOf<Module>())
        val result = ProjectGraphResult(graphModel, getConfig())

        assertThrows<IllegalArgumentException> {
            Mermaid.generateGraph(result)
        }
    }

    @Test
    fun `Given the LiveMatch App graph model, when generating graph, then it returns expected mermaid code`() {
        val graphModel = liveMatchReconstructedModel
        val config = getConfig(
            theme = Theme.BASE(
                mapOf(
                    "primaryTextColor" to "#fff",
                    "primaryColor" to "#5a4f7c",
                    "primaryBorderColor" to "#5a4f7c",
                    "lineColor" to "#f5a623",
                    "tertiaryColor" to "#40375c",
                    "fontSize" to "12px",
                ),
            ),
            orientation = Orientation.LEFT_TO_RIGHT,
        )

        val result = ProjectGraphResult(graphModel, config)
        val mermaidGraph = Mermaid.generateGraph(result)

        assertEquals(expectedLiveMatchGraph, mermaidGraph)
    }

    @Test
    fun `Given a module graph with theme settings, when generating graph, proper mermaid code is created`() {
        val graphModel = aModuleGraph()
        val config = getConfig(
            theme = Theme.BASE(
                focusColor = "#F5A622",
                themeVariables = mapOf(
                    "primaryTextColor" to "#F6F8FAff",
                    "primaryColor" to "#5a4f7c",
                    "primaryBorderColor" to "#5a4f7c",
                    "tertiaryColor" to "#40375c",
                    "lineColor" to "#f5a623",
                    "fontSize" to "12px",
                ),
            ),
            orientation = Orientation.TOP_TO_BOTTOM,
            focusedModulesRegex = ".*gama.*",
        )
        val result = ProjectGraphResult(graphModel, config)

        val mermaidGraph = Mermaid.generateGraph(result)

        assertEquals(expectedMermaidGraphCode, mermaidGraph)
    }
}
