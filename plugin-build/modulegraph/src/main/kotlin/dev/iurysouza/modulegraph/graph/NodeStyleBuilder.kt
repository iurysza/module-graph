package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.GraphOptions
import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.focusColor

internal object NodeStyleBuilder {
    /**
     * Generates Mermaid code to customize the styling of nodes based on their focus state and plugin type.
     *
     * @param digraphModel The list of digraph models containing source and target nodes.
     * @param options Configuration options including theme settings and whether to set styles by plugin type.
     * @return A `MermaidCode` containing the Mermaid syntax for highlighting the nodes.
     *
     * Example output of generated Mermaid code:
     * ```mermaid
     * classDef java fill:#C3E88D,stroke:#fff,stroke-width:2px,color:#fff;
     * class gama java;
     *
     * classDef focus fill:#F5A622,stroke:#fff,stroke-width:2px,color:#fff;
     * class alpha focus;
     * ```
     *Where "alpha" would be one of the focused nodes.
     */
    fun build(digraphModel: List<DigraphModel>, options: GraphOptions): MermaidCode {
        val distinctNodes = digraphModel
            .flatMap { listOf(it.source, it.target) }
            .distinctBy { it.name }
        val focusedNodes = distinctNodes.filter { it.isFocused }

        val pluginTypeStyling = applyStylingByPluginType(options, distinctNodes)
        val focusedNodesStyling = highlightFocusedNodes(focusedNodes, options.theme)
        return MermaidCode(
            buildString {
                if (pluginTypeStyling.isNotEmpty()) {
                    append(pluginTypeStyling + "\n")
                }
                append(focusedNodesStyling.value)
            },
        )
    }

    private fun applyStylingByPluginType(
        options: GraphOptions,
        nodeList: List<ModuleNode>,
    ): String {
        return if (options.setStyleByPluginType) {
            """
                |
                |${
            nodeList
                .distinctBy { it.plugin }
                .joinToString("\n") {
                    defineStyleClass(it.pluginClass(), it.plugin.color)
                }
            }
            """.trimMargin() + """
                |
                |${nodeList.joinToString("\n") { "class ${it.name} ${it.pluginClass()}" }}
            """.trimMargin()
        } else {
            ""
        }
    }

    private fun defineStyleClass(className: String, color: String) =
        """classDef $className fill:$color,stroke:#fff,stroke-width:2px,color:#fff;"""

    private fun ModuleNode.pluginClass() = plugin.id.split(".").takeLast(2).joinToString("_")

    private fun highlightFocusedNodes(
        nodeList: List<ModuleNode>,
        theme: Theme,
    ): MermaidCode = MermaidCode(
        if (nodeList.isEmpty()) {
            ""
        } else {
            """
               |
               |${defineStyleClass(FOCUS_CLASS_NAME, theme.focusColor())}
               |${nodeList.joinToString("\n") { "class ${it.name} $FOCUS_CLASS_NAME" }}
            """.trimMargin()
        },
    )

    private const val FOCUS_CLASS_NAME = "focus"
}
