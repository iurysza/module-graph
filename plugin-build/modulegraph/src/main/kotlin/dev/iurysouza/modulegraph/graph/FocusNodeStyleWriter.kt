package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.focusColor

object FocusNodeStyleWriter {
    /**
     * @param digraphModel The list of digraph models for which the highlighting is to be done.
     * @param theme The theme for the highlighting.
     * @return A `MermaidCode` with the code to highlight the focused nodes.
     *
     *This would generate a `MermaidCode` similar to:
     *```mermaid
     *classDef focus fill:#F5A622,stroke:#fff,stroke-width:2px,color:#fff;
     *class alpha focus
     *```
     *Where "alpha" would be one of the focused nodes.
     */
    fun highlightNode(digraphModel: List<DigraphModel>, theme: Theme): MermaidCode {
        val focusedNodes = digraphModel
            .flatMap { listOf(it.source, it.target) }
            .distinctBy { it.name }
            .filter { it.isFocused }

        return toCode(focusedNodes, theme)
    }

    private fun toCode(
        nodeList: List<ModuleNode>,
        theme: Theme,
    ): MermaidCode = MermaidCode(
        if (nodeList.isEmpty()) {
            ""
        } else {
            """
               |
               |classDef $FOCUS_CLASS_NAME fill:${theme.focusColor()},stroke:#fff,stroke-width:2px,color:#fff;
               |${nodeList.joinToString("\n") { "class ${it.name} $FOCUS_CLASS_NAME" }}
           """.trimMargin()
        }
    )

    private const val FOCUS_CLASS_NAME = "focus"
}

