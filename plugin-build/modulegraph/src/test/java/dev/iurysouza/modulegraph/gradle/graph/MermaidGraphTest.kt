package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.GraphOptions
import dev.iurysouza.modulegraph.Mermaid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MermaidGraphTest {

    @Test
    fun `digraph builder works as expected`() {
        val dependencies = aModuleGraph()
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

        val mermaidGraph = Mermaid.generateGraph(dependencies, graphOptions)

        assertEquals(expectedMermaidGraphCode, mermaidGraph)
    }

}
