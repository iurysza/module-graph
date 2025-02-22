package dev.iurysouza.modulegraph.graph.subgraph

import dev.iurysouza.modulegraph.graph.DigraphModel
import dev.iurysouza.modulegraph.graph.MermaidCode
import dev.iurysouza.modulegraph.model.GraphConfig

internal object SubgraphBuilder {
    internal fun build(
        digraphModels: List<DigraphModel>,
        config: GraphConfig,
    ): MermaidCode = when {
        config.nestingEnabled -> NestedSubgraphBuilder.build(digraphModels)
        else -> FlatSubgraphBuilder.build(digraphModels, config.showFullPath)
    }
}
