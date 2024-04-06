package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.*
import dev.iurysouza.modulegraph.Dependency
import dev.iurysouza.modulegraph.Mermaid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MermaidGraphTest {

    @Test
    fun `plugin adds custom styling to focused modules `() {
        val reconstructedModel = mapOf(
            ":example" to listOf(
                Dependency(
                    targetProjectPath = ":groupFolder:example2",
                    configName = "implementation"
                ),
                Dependency(
                    targetProjectPath = ":groupFolder:example3",
                    configName = "implementation"
                )
            )
        )
        val focusColor = "#F5A622"
        val graphOptions = someGraphOptions(
            regex = ".*example2.*".toRegex(),
            theme = Theme.BASE(
                focusColor = focusColor,
            ),
            showFullPath = true
        )

        val mermaidGraph = Mermaid.generateGraph(reconstructedModel, graphOptions)

        assertEquals(
            """
                ```mermaid
                %%{
                  init: {
                    'theme': 'base'
                  }
                }%%

                graph LR
                  :example --> :groupFolder:example2

                classDef focus fill:${focusColor},stroke:#fff,stroke-width:2px,color:#fff;
                class :groupFolder:example2 focus
                ```
            """.trimIndent(), mermaidGraph
        )
    }

    @Test
    fun `a single module project will throw an error`() {
        val graphModel = mapOf(
            ":example" to listOf<Dependency>()
        )

        val graphOptions = someGraphOptions()

        assertThrows<IllegalArgumentException> {
            Mermaid.generateGraph(graphModel, graphOptions)
        }

    }
    @Test
    fun `livematch graph works`() {
        val graphModel = liveMatchReconstructedModel
        val graphOptions = GraphOptions(
            linkText = LinkText.NONE,
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
            showFullPath = false,
            orientation = Orientation.LEFT_TO_RIGHT,
        )

        val mermaidGraph = Mermaid.generateGraph(graphModel, graphOptions)

        assertEquals(expectedLiveMatchGraph, mermaidGraph)
    }

    @Test
    fun `digraph builder works as expected`() {
        val graphModel = aModuleGraph()
        val graphOptions = GraphOptions(
            linkText = LinkText.NONE,
            theme = Theme.BASE(
                focusColor = "#F5A622",
                themeVariables = mapOf(
                    "primaryTextColor" to "#F6F8FAff",
                    "primaryColor" to "#5a4f7c",
                    "primaryBorderColor" to "#5a4f7c",
                    "tertiaryColor" to "#40375c",
                    "lineColor" to "#f5a623",
                    "fontSize" to "12px"
                ),
            ),
            showFullPath = false,
            pattern = ".*gama.*".toRegex(),
            orientation = Orientation.TOP_TO_BOTTOM,
        )

        val mermaidGraph = Mermaid.generateGraph(graphModel, graphOptions)

        assertEquals(expectedMermaidGraphCode, mermaidGraph)
    }

}
