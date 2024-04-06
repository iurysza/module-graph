package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.*
import dev.iurysouza.modulegraph.Dependency
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SubgraphBuilderTest {
    @Test
    fun `new example works as expected`() {
        val graphModel = mapOf(
            ":example" to listOf(
                Dependency(
                    targetProjectPath = ":groupFolder:example2",
                    configName = "implementation",
                ),
            ),
        )

        val digraph = DigraphBuilder.build(graphModel, someGraphOptions())
        val subgraph = SubgraphBuilder.build(digraph, showFullPath = false).value
        val expectedSubgraphSyntax = """
            |  subgraph groupFolder
            |    example2
            |  end
        """.trimMargin()
        assertEquals(expectedSubgraphSyntax, subgraph)
    }

    @Test
    fun `when showFullPath is true, an empty graph is generated`() {
        val graphModel = aModuleGraph()
        val graphOptions = GraphOptions(
            linkText = LinkText.NONE,
            theme = Theme.NEUTRAL,
            showFullPath = false,
            pattern = ".*gama.*".toRegex(),
            orientation = Orientation.TOP_TO_BOTTOM,
        )

        val digraph = DigraphBuilder.build(graphModel, graphOptions)
        val subgraph = SubgraphBuilder.build(digraph, showFullPath = true)
        assertEquals("", subgraph.value)
    }

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
        val subgraph = SubgraphBuilder.build(digraph, showFullPath = false)
        val expectedSubgraphSyntax = """
            |  subgraph container
            |    gama
            |  end
            |  subgraph sample
            |    alpha
            |    zeta
            |  end
        """.trimMargin()
        assertEquals(expectedSubgraphSyntax, subgraph.value)
    }
}
