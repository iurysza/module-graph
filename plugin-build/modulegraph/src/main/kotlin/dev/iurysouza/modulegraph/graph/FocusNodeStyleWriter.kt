package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.focusColor

object FocusNodeStyleWriter {
    fun highlightNode(digraphModel: List<DigraphModel>, theme: Theme): MermaidSyntax {
        val focusedNodes = digraphModel
            .flatMap { listOf(it.source, it.target) }
            .distinctBy { it.name }
            .filter { it.isFocused }

        return toCode(focusedNodes, theme)
    }

    private fun toCode(
        nodeList: List<ModuleNode>,
        theme: Theme,
    ): MermaidSyntax = MermaidSyntax(
        if (nodeList.isEmpty()) {
            ""
        } else {
            """
               |classDef $FOCUS_CLASS_NAME fill:${theme.focusColor()},stroke:#fff,stroke-width:2px,color:#fff;
               |${nodeList.joinToString("\n") { "class ${it.name} $FOCUS_CLASS_NAME" }}
           """.trimMargin()
        }
    )

    private const val FOCUS_CLASS_NAME = "focus"
}

