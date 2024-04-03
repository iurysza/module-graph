package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.LinkText

object DigraphCodeGenerator {
    fun mermaid(
        digraphModel: List<DigraphModel>,
        linkText: LinkText,
    ): MermaidSyntax = MermaidSyntax(
        """
        |${digraphModel.joinToString("\n") { toMermaid(it, linkText) }}
    """.trimMargin()
    )

    private fun toMermaid(it: DigraphModel, linkText: LinkText): CharSequence = """
        |  ${it.source.name} ${linkText.toLinkString(it.target.config.value)} ${it.target.name}
    """.trimMargin()
}

private fun LinkText.toLinkString(configName: String?): String = when (this) {
    LinkText.CONFIGURATION -> "-- $configName -->"
    LinkText.NONE -> "-->"
}
