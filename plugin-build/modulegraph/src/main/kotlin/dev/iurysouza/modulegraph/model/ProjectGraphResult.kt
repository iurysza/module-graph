package dev.iurysouza.modulegraph.model

import dev.iurysouza.modulegraph.model.alias.ProjectGraph
import java.io.Serializable

internal data class ProjectGraphResult(
    /** The output graph model after processing */
    val graph: ProjectGraph,
    /** The original config provided used to produce [graph] */
    val config: SingleGraphConfig,
) : Serializable
