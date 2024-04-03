package dev.iurysouza.modulegraph

import dev.iurysouza.modulegraph.graph.*

internal object Mermaid {

    fun generateGraph(
        input: Map<String, List<Dependency>>,
        graphOptions: GraphOptions,
    ): String {
        val (linkText, theme, orientation) = graphOptions
        val digraph = DigraphBuilder.build(input, graphOptions)
        val configSyntax = ConfigCodeGenerator.createConfig(theme)
        val subgraphSyntax = if (graphOptions.showFullPath) {
            MermaidSyntax()
        } else {
            SubgraphBuilder.build(digraph)
        }
        val digraphSyntax = DigraphCodeGenerator.mermaid(digraph, linkText)
        val highlightSyntax = FocusNodeStyleWriter.highlightNode(digraph, theme)
        return """
                |${configSyntax.value}
                |
                |graph ${orientation.value}
                |${subgraphSyntax.value}
                |${digraphSyntax.value}
                |
                |${highlightSyntax.value}
            """.trimMargin()
    }
}

data class GraphOptions(
    val linkText: LinkText,
    val theme: Theme,
    val orientation: Orientation,
    val pattern: Regex,
    val showFullPath: Boolean,
)
