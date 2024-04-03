package dev.iurysouza.modulegraph

import dev.iurysouza.modulegraph.gradle.graph.*
import dev.iurysouza.modulegraph.gradle.graph.DigraphBuilder
import dev.iurysouza.modulegraph.gradle.graph.DigraphInput

internal object Mermaid {

    fun generateGraph(
        input: DigraphInput,
        graphOptions: GraphOptions,
    ): String {
        val (linkText, theme, orientation) = graphOptions
        val digraph = DigraphBuilder.build(input)
        val configSyntax = ConfigCodeGenerator.createConfig(theme)
        val subgraphSyntax = if (input.showFullPath) {
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
)
