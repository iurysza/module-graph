package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.LinkText

internal object DigraphCodeBuilder {

    /**
     * Converts a list of digraph models into a Mermaid syntax.
     *
     * @param digraphModel The list of digraph models
     * @param linkText The text for the link between nodes in the graph.
     * @return A `MermaidCode` representing the list of digraphs.
     *
     *This would generate a `MermaidCode` similar to:
     *
     *```mermaid
     * alpha --> gama
     * gama -- implementation --> zeta
     *```
     */
    fun build(
        digraphModel: List<DigraphModel>,
        linkText: LinkText,
    ): MermaidCode = MermaidCode(
        """
        |${digraphModel.joinToString("\n") { toMermaid(it, linkText) }}
        """.trimMargin(),
    )

    private fun toMermaid(it: DigraphModel, linkText: LinkText): CharSequence = """
        |  ${it.source.name} ${linkText.toLinkString(it.target.config.value)} ${it.target.name}
    """.trimMargin()
}

private fun LinkText.toLinkString(configName: String?): String = when (this) {
    LinkText.CONFIGURATION -> "-- $configName -->"
    LinkText.NONE -> "-->"
}
