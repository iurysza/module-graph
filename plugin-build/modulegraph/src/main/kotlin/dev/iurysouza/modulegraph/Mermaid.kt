package dev.iurysouza.modulegraph

import dev.iurysouza.modulegraph.gradle.Module
import dev.iurysouza.modulegraph.graph.*

internal object Mermaid {

    fun generateGraph(
        graphModel: Map<Module, List<Module>>,
        graphOptions: GraphOptions,
    ): String {
        val (linkText, theme, orientation) = graphOptions
        val digraph = DigraphBuilder.build(graphModel, graphOptions)

        val configCode = ConfigCodeBuilder.build(theme)
        val subgraphCode = SubgraphBuilder.build(digraph, graphOptions.showFullPath)
        val digraphCode = DigraphCodeBuilder.build(digraph, linkText)
        val highlightCode = NodeStyleBuilder.build(digraph, graphOptions)

        return buildString {
            append("```mermaid")
            lineBreak()
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
            lineBreak()
            append("```")
        }
    }

    private fun StringBuilder.appendCode(code: MermaidCode) {
        append(code.value)
    }
}

internal data class GraphOptions(
    val linkText: LinkText,
    val theme: Theme,
    val orientation: Orientation,
    val focusedNodesRegex: Regex? = null,
    val showFullPath: Boolean,
    val setStyleByModuleType: Boolean,
)
