package dev.iurysouza.modulegraph.gradle.graphparser.model

internal data class GradleProjectConfiguration(
    val name: String,
    /** The paths of all the projects which are dependencies in this configuration */
    val projectPaths: List<ProjectPath>,
)
