package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.gradle.graph.DigraphBuilder
import dev.iurysouza.modulegraph.gradle.graph.SubgraphBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SubgraphBuilderTest {

    @Test
    fun `works as expected`() {
        val input = aDigraphInput(regex = ".*gama.*")
        val digraph = DigraphBuilder.build(input)
        val subgraph = SubgraphBuilder.build(digraph)
        val expectedSubgraphSyntax = """
            |subgraph sample
            |  alpha
            |  zeta
            |end
            |subgraph container
            |  gama
            |end
""".trimMargin()
        assertEquals(expectedSubgraphSyntax, subgraph.value)
    }
}
