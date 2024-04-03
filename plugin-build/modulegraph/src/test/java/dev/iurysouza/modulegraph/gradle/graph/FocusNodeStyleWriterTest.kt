package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.FocusColor
import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.graph.DigraphBuilder
import dev.iurysouza.modulegraph.graph.FocusNodeStyleWriter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FocusNodeStyleWriterTest {

    private fun expectedHighlightCode(focusedNode: String, focusColor: FocusColor) = """
        |classDef focus fill:${focusColor},stroke:#fff,stroke-width:2px,color:#fff;
        |class $focusedNode focus
    """.trimMargin()

    @Test
    fun `code highlighter works as expected`() {
        val theme = Theme.BASE(focusColor = "#F5A622")
        val focusedNode = "gama"
        val input = aDigraphInput(regex = ".*$focusedNode.*")

        val digraph = DigraphBuilder.build(input)
        val highlightSyntax = FocusNodeStyleWriter.highlightNode(digraph, theme)


        assertEquals(expectedHighlightCode(focusedNode, theme.focusColor), highlightSyntax.value)
    }
}
