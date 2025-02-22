package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.ModuleType

internal data class DigraphModel(
    val source: ModuleNode,
    val target: ModuleNode,
)

internal data class ModuleNode(
    val name: String,
    val isFocused: Boolean,
    val fullPath: String,
    val config: ModuleConfig,
    val type: ModuleType,
    val parent: String,
) {
    val pathSegments: List<String> get() = fullPath.split(':').filter { it.isNotEmpty() }
}

internal data class PathNode(
    val name: String,
    val fullPath: String,
    val children: MutableMap<String, PathNode> = mutableMapOf(),
    val modules: MutableList<ModuleNode> = mutableListOf(),
)

@JvmInline
internal value class ModuleConfig(val value: String) {
    companion object {
        fun none() = ModuleConfig("none")
    }
}

@JvmInline
internal value class MermaidCode(val value: String = "") {
    fun isNotEmpty(): Boolean = value.isNotEmpty()

    companion object {
        val EMPTY = MermaidCode("")
    }
}
