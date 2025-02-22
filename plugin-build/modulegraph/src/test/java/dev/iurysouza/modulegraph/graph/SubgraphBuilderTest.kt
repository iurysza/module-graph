package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.gradle.Module
import dev.iurysouza.modulegraph.graph.subgraph.FlatSubgraphBuilder
import dev.iurysouza.modulegraph.model.GraphParseResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SubgraphBuilderTest {

    @Test
    fun `Subgraph with default options produces correct structure`() {
        val graphModel = mapOf(
            Module(":example") to listOf(
                Module(
                    path = ":groupFolder:example2",
                    configName = "implementation",
                ),
            ),
        )
        val config = getConfig()
        val result = GraphParseResult(graphModel, config)

        val subgraph = FlatSubgraphBuilder.build(
            list = DigraphBuilder.build(result),
            showFullPath = false,
        ).value

        val expectedSubgraphSyntax = """
            |  subgraph :groupFolder
            |    :groupFolder:example2["example2"]
            |  end
        """.trimMargin()
        assertEquals(expectedSubgraphSyntax, subgraph)
    }

    @Test
    fun `when showFullPath is true, an empty graph is generated`() {
        val graphModel = aModuleGraph()
        val config = getConfig(
            focusedModulesRegex = ".*gama.*",
            orientation = Orientation.TOP_TO_BOTTOM,
        )
        val result = GraphParseResult(graphModel, config)

        val subgraph = FlatSubgraphBuilder.build(
            list = DigraphBuilder.build(result),
            showFullPath = true,
        )

        assertEquals("", subgraph.value)
    }

    @Test
    fun `Subgraph partitions correctly based on given module configuration`() {
        val graphModel = aModuleGraph()
        val config = getConfig(
            focusedModulesRegex = ".*gama.*",
            orientation = Orientation.TOP_TO_BOTTOM,
        )
        val result = GraphParseResult(graphModel, config)

        val subgraph = FlatSubgraphBuilder.build(
            list = DigraphBuilder.build(result),
            showFullPath = false,
        )

        val expectedSubgraphSyntax = """
            |  subgraph :sample
            |    :sample:alpha["alpha"]
            |    :sample:zeta["zeta"]
            |  end
            |  subgraph :sample:container
            |    :sample:container:gama["gama"]
            |  end
        """.trimMargin()
        assertEquals(expectedSubgraphSyntax, subgraph.value)
    }
}
