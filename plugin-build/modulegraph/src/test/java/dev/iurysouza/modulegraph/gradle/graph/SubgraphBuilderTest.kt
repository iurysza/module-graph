package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.*
import dev.iurysouza.modulegraph.Dependency
import dev.iurysouza.modulegraph.graph.DigraphBuilder
import dev.iurysouza.modulegraph.graph.SubgraphBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SubgraphBuilderTest {
    @Test
    fun `new example works as expected`() {
        val graphModel = mapOf(
            ":example" to listOf(
                Dependency(
                    targetProjectPath = ":groupFolder:example2",
                    configName = "implementation"
                )
            )
        )

        val digraph = DigraphBuilder.build(graphModel, someGraphOptions())
        val subgraph = SubgraphBuilder.build(digraph).value
        val expectedSubgraphSyntax = """
            |  subgraph groupFolder
            |    example2
            |  end
""".trimMargin()
        assertEquals(expectedSubgraphSyntax, subgraph)
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
