package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.GraphOptions
import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.graph.DigraphBuilder
import dev.iurysouza.modulegraph.graph.SubgraphBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SubgraphBuilderTest {

    @Test
    fun `works as expected`() {
        val graphModel = aModuleGraph()
        val graphOptions = GraphOptions(
            linkText = LinkText.NONE,
            theme = Theme.NEUTRAL,
            showFullPath = false,
            pattern = ".*gama.*".toRegex(),
            orientation = Orientation.TOP_TO_BOTTOM,
        )

        val digraph = DigraphBuilder.build(graphModel, graphOptions)
        val subgraph = SubgraphBuilder.build(digraph)
        val expectedSubgraphSyntax = """
            |  subgraph sample
            |    alpha
            |    zeta
            |  end
            |  subgraph container
            |    gama
            |  end
""".trimMargin()
        assertEquals(expectedSubgraphSyntax, subgraph.value)
    }
}
