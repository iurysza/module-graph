package dev.iurysouza.modulegraph.gradle.graph

object SubgraphBuilder {

    fun build(list: List<DigraphModel>): MermaidSyntax {
        val subGraphs = groupDigraphsByParent(list)
        return toMermaidSyntax(subGraphs)
    }

    private fun toMermaidSyntax(subgraphModel: List<Pair<String, List<ModuleNode>>>): MermaidSyntax {
        val subgraph = subgraphModel.joinToString("\n") { (parent, children) ->
            val childrenNames = children.joinToString("\n") { "    ${it.name}" }
            """|  subgraph $parent
               |$childrenNames
               |  end""".trimMargin()
        }
        return MermaidSyntax(subgraph)
    }

    private fun groupDigraphsByParent(
        modelList: List<DigraphModel>,
    ): List<Pair<String, List<ModuleNode>>> = modelList
        .flatMap { listOf(it.source, it.target) }
        .groupBy { it.parent }
        .map { (parent, children) -> parent to children.distinctBy { it.name } }

}
