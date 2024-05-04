package dev.iurysouza.modulegraph.gradle.graphparser.model

import dev.iurysouza.modulegraph.ModuleType
import dev.iurysouza.modulegraph.getModuleType
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

internal data class GradleProjectImpl(val project: Project) : GradleProject {
    override val path = project.path
    override val configurations: List<GradleProjectConfiguration> =
        project.configurations.map { config ->
            val subprojects = config.dependencies
                .withType(ProjectDependency::class.java)
                .map { subproject ->
                    GradleProjectImpl(subproject.dependencyProject)
                }
            GradleProjectConfiguration(config.name, subprojects)
        }

    override fun getModuleType(customModuleTypes: List<ModuleType>) =
        project.getModuleType(customModuleTypes)
}
