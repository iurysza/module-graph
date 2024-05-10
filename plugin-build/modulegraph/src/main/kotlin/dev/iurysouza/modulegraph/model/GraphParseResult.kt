package dev.iurysouza.modulegraph.model

import dev.iurysouza.modulegraph.model.alias.ProjectGraph

internal data class GraphParseResult(
    /** The output graph model after parsing */
    val graph: ProjectGraph,
    /** The original config provided used to produce [graph] */
    val config: GraphConfig,
)
