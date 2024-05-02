package dev.iurysouza.modulegraph.gradle.graphparser.model

internal data class GradleProjectConfiguration(
    val name: String,
    /** All the projects which are dependencies in this configuration */
    val projects: List<GradleProject>
)
