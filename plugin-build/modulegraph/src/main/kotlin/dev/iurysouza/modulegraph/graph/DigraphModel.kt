package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.Dependency

internal data class DigraphInput(
    val dependencies: Map<String, List<Dependency>>,
    val pattern: Regex,
    val showFullPath: Boolean,
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
    val parent: String,
)

@JvmInline
value class ModuleConfig(val value: String) {
    companion object {
        fun none() = ModuleConfig("none")
    }
}

@JvmInline
value class MermaidCode(val value: String = "")

