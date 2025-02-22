package dev.iurysouza.modulegraph

import dev.iurysouza.modulegraph.graph.*
import dev.iurysouza.modulegraph.graph.subgraph.SubgraphBuilder
import dev.iurysouza.modulegraph.model.GraphParseResult

internal object Mermaid {
    fun generateGraph(
        result: GraphParseResult,
    ): String {
        val config = result.config
        val digraph = DigraphBuilder.build(result)

        val configCode = ConfigCodeBuilder.build(config.theme)
        val subgraphCode = SubgraphBuilder.build(digraph, config)
        val digraphCode = DigraphCodeBuilder.build(digraph, config.linkText)
        val highlightCode = NodeStyleBuilder.build(digraph, config)
        val orientationName = config.orientation.value

        return buildString {
            append("```mermaid")
            lineBreak()
            appendCode(configCode)
            lineBreak()
            lineBreak()
            append("graph $orientationName")
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
