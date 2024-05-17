package dev.iurysouza.modulegraph.model

import dev.iurysouza.modulegraph.model.alias.ProjectGraph
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
internal data class GraphParseResult(
    /** The output graph model after parsing */
    val graph: ProjectGraph,
    /** The original config provided used to produce [graph] */
    val config: GraphConfig,
) : JavaSerializable
