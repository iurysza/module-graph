package dev.iurysouza.modulegraph.gradle

import java.io.Serializable

/**
 * Represents a dependency on a project.
 * Contains the name of the configuration to which the dependency belongs.
 */
internal data class Dependency(
    val path: String,
    val configName: String? = null,
    val plugin: PluginType = PluginType.Java(),
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 46465844
    }
}
