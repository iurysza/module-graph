package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.*
import dev.iurysouza.modulegraph.graph.DigraphBuilder
import dev.iurysouza.modulegraph.graph.FocusNodeStyleBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FocusNodeStyleWriterTest {

    private fun expectedHighlightCode(focusedNode: String, focusColor: FocusColor) = """
        |
        |classDef focus fill:${focusColor},stroke:#fff,stroke-width:2px,color:#fff;
        |class $focusedNode focus
    """.trimMargin()

    @Test
    fun `code highlighter works as expected`() {
        val theme = Theme.BASE(focusColor = "#F5A622")
        val focusedNode = "gama"
        val graphOptions = GraphOptions(
            linkText = LinkText.NONE,
            theme = theme,
            showFullPath = false,
            pattern = ".*$focusedNode.*".toRegex(),
            orientation = Orientation.TOP_TO_BOTTOM,
        )

        val digraph = DigraphBuilder.build(aModuleGraph(), graphOptions)
        val highlightSyntax = FocusNodeStyleBuilder.build(digraph, theme)

        assertEquals(expectedHighlightCode(focusedNode, theme.focusColor), highlightSyntax.value)
    }
}
