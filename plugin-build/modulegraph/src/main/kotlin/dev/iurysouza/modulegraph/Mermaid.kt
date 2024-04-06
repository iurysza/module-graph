package dev.iurysouza.modulegraph

import dev.iurysouza.modulegraph.graph.*

internal object Mermaid {

    fun generateGraph(
        graphModel: Map<String, List<Dependency>>,
        graphOptions: GraphOptions,
    ): String {
        val (linkText, theme, orientation) = graphOptions
        val digraph = DigraphBuilder.build(graphModel, graphOptions)

        val configCode = ConfigCodeBuilder.build(theme)
        val subgraphCode = SubgraphBuilder.build(digraph, graphOptions.showFullPath)
        val digraphCode = DigraphCodeBuilder.build(digraph, linkText)
        val highlightCode = FocusNodeStyleBuilder.build(digraph, theme)

        return buildString {
            appendCode(configCode)
            lineBreak()
            lineBreak()
            append("graph ${orientation.value}")
            if (subgraphCode.isNotEmpty()) {
                lineBreak()
                appendCode(subgraphCode)
            }
            lineBreak()
            appendCode(digraphCode)
            if (highlightCode.isNotEmpty()) {
                lineBreak()
                appendCode(highlightCode)
            }
        }
    }

    private fun StringBuilder.appendCode(code: MermaidCode) {
        append(code.value)
    }

    private fun StringBuilder.lineBreak() {
        append("\n")
    }
}

data class GraphOptions(
    val linkText: LinkText,
    val theme: Theme,
    val orientation: Orientation,
    val pattern: Regex,
    val showFullPath: Boolean,
)
