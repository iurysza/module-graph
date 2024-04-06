package dev.iurysouza.modulegraph.graph

internal data class DigraphModel(
    val source: ModuleNode,
    val target: ModuleNode,
)

internal data class ModuleNode(
    val name: String,
    val isFocused: Boolean,
    val fullName: String,
    val config: ModuleConfig,
    val parent: String,
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

