package dev.iurysouza.modulegraph.gradle.graphparser.model

import java.io.Serializable as JavaSerializable

/**
 * A serializable snapshot of a single Gradle project's data.
 *
 * Each project contributes one instance to the
 * [dev.iurysouza.modulegraph.gradle.ModuleGraphRegistry].
 */
internal data class ProjectInfo(
    /** The Gradle path of the project, e.g. ":app:feature". */
    val path: ProjectPath,
    /**
     * The ids of the plugins applied to the project, used to infer its
     * [dev.iurysouza.modulegraph.ModuleType].
     */
    val pluginIds: List<String>,
    /**
     * The "group:name" coordinates of external dependencies, used to infer its
     * [dev.iurysouza.modulegraph.ModuleType].
     */
    val externalDependencies: List<String>,
    /** The project dependencies declared per configuration. */
    val configurations: List<GradleProjectConfiguration>,
) : JavaSerializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
