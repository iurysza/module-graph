package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.GraphOptions
import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.focusColor

internal object NodeStyleBuilder {
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
    fun build(digraphModel: List<DigraphModel>, options: GraphOptions): MermaidCode {
        val distinctNodes = digraphModel
            .flatMap { listOf(it.source, it.target) }
            .distinctBy { it.name }
        val focusedNodes = distinctNodes.filter { it.isFocused }

        val pluginTypeStyling = customizeNodeColorsByPluginType(options, distinctNodes)
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

    private fun customizeNodeColorsByPluginType(
        options: GraphOptions,
        nodeList: List<ModuleNode>,
    ): String {
        return if (options.styleNodeByPluginType) {
            """
                |
                |${
            nodeList.distinctBy { it.plugin }.joinToString("\n") {
                """
                    |classDef ${it.pluginClass()} fill:${it.plugin.color},stroke:#fff,stroke-width:2px,color:#fff;
                """.trimMargin()
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
               |classDef $FOCUS_CLASS_NAME fill:${theme.focusColor()},stroke:#fff,stroke-width:2px,color:#fff;
               |${nodeList.joinToString("\n") { "class ${it.name} $FOCUS_CLASS_NAME" }}
            """.trimMargin()
        },
    )

    private const val FOCUS_CLASS_NAME = "focus"
}
