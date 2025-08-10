package dev.iurysouza.modulegraph.gradle.graphparser.projectquerier

import dev.iurysouza.modulegraph.ModuleType
import dev.iurysouza.modulegraph.getModuleType
import dev.iurysouza.modulegraph.gradle.graphparser.model.GradleProjectConfiguration
import dev.iurysouza.modulegraph.gradle.graphparser.model.ProjectPath
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

internal class GradleProjectQuerier(private val allProjects: Set<Project>) : ProjectQuerier {
    override fun getProjectType(
        projectPath: ProjectPath,
        customModuleTypes: List<ModuleType>,
    ): ModuleType {
        val project = getProject(projectPath) ?: return ModuleType.Unknown()
        return project.getModuleType(customModuleTypes)
    }

    override fun getConfigurations(projectPath: ProjectPath): List<GradleProjectConfiguration> {
        val project = getProject(projectPath) ?: return emptyList()
        return project.configurations.map { config ->
            val projectPaths = config.dependencies
                .withType(ProjectDependency::class.java)
                .map { subproject -> subproject.path }
            GradleProjectConfiguration(config.name, projectPaths)
        }
    }

    private fun getProject(path: ProjectPath) = allProjects.firstOrNull { it.path == path }
}
