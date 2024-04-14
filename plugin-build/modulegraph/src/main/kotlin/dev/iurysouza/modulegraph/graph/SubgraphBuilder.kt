package dev.iurysouza.modulegraph.graph

internal object SubgraphBuilder {

    /**
     * The `build` function groups digraph models by parent and generates mermaid syntax for subgraphs.
     *
     * @param list The list of digraph models to be grouped by parent.
     * @return The resulting `MermaidCode` corresponding to the structured digraphs.
     *
     * eg.:
     *```
     *  subgraph sample
     *    alpha
     *    zeta
     *  end
     *  subgraph container
     *    gama
     *  end
     *```
     *
     *Where "sample" and "container" are parent nodes, and "alpha", "zeta", and "gama" are children.
     */
    fun build(list: List<DigraphModel>, showFullPath: Boolean): MermaidCode {
        if (showFullPath) return MermaidCode.EMPTY
        val subGraphs = groupDigraphsByParent(list)
        return toMermaidCode(subGraphs)
    }

    private fun toMermaidCode(
        subgraphModel: List<Pair<String, List<ModuleNode>>>,
    ): MermaidCode = MermaidCode(
        subgraphModel.joinToString("\n") { (parent, children) ->
            val childrenNames = children.joinToString("\n") { "    ${it.name}" }
            """|  subgraph $parent
               |$childrenNames
               |  end
            """.trimMargin()
        }.trimMargin(),
    )

    private fun groupDigraphsByParent(
        modelList: List<DigraphModel>,
    ): List<Pair<String, List<ModuleNode>>> = modelList.asSequence()
        .flatMap { listOf(it.source, it.target) }
        .filter { it.parent.isNotEmpty() }
        .groupBy { it.parent }
        .map { (parent, children) -> parent to children.distinctBy { it.name } }
        .sortedBy { it.first }
        .toList()
}
