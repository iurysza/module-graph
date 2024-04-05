package dev.iurysouza.modulegraph

import dev.iurysouza.modulegraph.graph.*

internal object Mermaid {

    fun generateGraph(
        graphModel: Map<String, List<Dependency>>,
        graphOptions: GraphOptions,
    ): String {
        val (linkText, theme, orientation) = graphOptions
        val digraph = DigraphBuilder.build(graphModel, graphOptions)

        val subgraphSyntax = if (graphOptions.showFullPath) {
            MermaidCode()
        } else {
            SubgraphBuilder.build(digraph)
        }
        val digraphSyntax = DigraphCodeGenerator.mermaid(digraph, linkText)
        val highlightSyntax = FocusNodeStyleWriter.highlightNode(digraph, theme)
        val configSyntax = ConfigCodeGenerator.createConfig(theme)
        return buildString {
            append(configSyntax.value)
            lineBreak()
            lineBreak()
            append("graph ${orientation.value}")
            if (!subgraphSyntax.isEmpty()) {
                lineBreak()
                append(subgraphSyntax.value)
            }
            lineBreak()
            append(digraphSyntax.value)
            if (!highlightSyntax.isEmpty()) {
                lineBreak()
                append(highlightSyntax.value)
            }
        }
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
