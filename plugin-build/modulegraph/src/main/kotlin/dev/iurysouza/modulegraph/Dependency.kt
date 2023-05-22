package dev.iurysouza.modulegraph

import java.io.Serializable

/**
 * Represents a dependency on a project.
 * Contains the name of the configuration to which the dependency belongs.
 */
internal data class Dependency(
    val targetProjectPath: String,
    val configName: String,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 46465844
    }
}
