package dev.iurysouza.modulegraph

import dev.iurysouza.modulegraph.graph.*

internal object Mermaid {

    fun generateGraph(
        graphModel: Map<String, List<Dependency>>,
        graphOptions: GraphOptions,
    ): String {
        val (linkText, theme, orientation) = graphOptions
        val digraph = DigraphBuilder.build(graphModel, graphOptions)

        val configSyntax = ConfigCodeGenerator.createConfig(theme)
        val subgraphSyntax = SubgraphBuilder.build(digraph, graphOptions.showFullPath)
        val digraphSyntax = DigraphCodeGenerator.mermaid(digraph, linkText)
        val highlightSyntax = FocusNodeStyleWriter.highlightNode(digraph, theme)

        return buildString {
            appendCode(configSyntax)
            lineBreak()
            lineBreak()
            append("graph ${orientation.value}")
            if (subgraphSyntax.isNotEmpty()) {
                lineBreak()
                appendCode(subgraphSyntax)
            }
            lineBreak()
            appendCode(digraphSyntax)
            if (highlightSyntax.isNotEmpty()) {
                lineBreak()
                appendCode(highlightSyntax)
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
