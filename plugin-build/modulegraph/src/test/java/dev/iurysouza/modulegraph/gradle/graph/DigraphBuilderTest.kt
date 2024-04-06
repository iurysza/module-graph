package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.GraphOptions
import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.graph.DigraphBuilder
import dev.iurysouza.modulegraph.graph.DigraphCodeBuilder
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertEquals

class DigraphBuilderTest {

    @Test
    fun `digraph builder works as expected`() {

        val graphModel = aModuleGraph()
        val graphOptions = GraphOptions(
            linkText = LinkText.NONE,
            theme = Theme.NEUTRAL,
            showFullPath = false,
            pattern = ".*gama.*".toRegex(),
            orientation = Orientation.TOP_TO_BOTTOM,
        )

        val digraphModelList = DigraphBuilder.build(graphModel, graphOptions)
        val digraphSyntax = DigraphCodeBuilder.build(digraphModelList, graphOptions.linkText)

        val mermaidStringSyntax = """
        |  alpha --> gama
        |  gama --> zeta""".trimMargin()
        assertEquals(mermaidStringSyntax, digraphSyntax.value)
    }
}
