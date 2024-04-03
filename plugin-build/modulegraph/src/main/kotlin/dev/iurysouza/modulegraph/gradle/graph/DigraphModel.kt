package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.Dependency
import dev.iurysouza.modulegraph.LinkText

internal data class DigraphInput(
    val pattern: Regex,
    val dependencies: Map<String, List<Dependency>>,
    val showFullPath: Boolean,
    val linkText: LinkText,
)

data class DigraphModel(
    val source: ModuleNode,
    val target: ModuleNode,
)

data class ModuleNode(
    val name: String,
    val isFocused: Boolean,
    val fullName: String,
    val config: ModuleConfig,
)

@JvmInline
value class ModuleConfig(val value: String) {
    companion object {
        fun none() = ModuleConfig("none")
    }
}

@JvmInline
value class MermaidSyntax(val value: String)
