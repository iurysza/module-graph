package dev.iurysouza.modulegraph.gradle.graphparser.projectquerier

import dev.iurysouza.modulegraph.ModuleType
import dev.iurysouza.modulegraph.gradle.graphparser.model.GradleProjectConfiguration
import dev.iurysouza.modulegraph.gradle.graphparser.model.ProjectInfo
import dev.iurysouza.modulegraph.gradle.graphparser.model.ProjectPath
import dev.iurysouza.modulegraph.resolveModuleType

/** [ProjectQuerier] backed by the [ProjectInfo] snapshot collected from each project. */
internal class SnapshotProjectQuerier(
    private val infoByPath: Map<ProjectPath, ProjectInfo>,
) : ProjectQuerier {
    override fun getProjectType(
        projectPath: ProjectPath,
        customModuleTypes: List<ModuleType>,
    ): ModuleType {
        val info = infoByPath[projectPath] ?: return ModuleType.Unknown()
        return resolveModuleType(
            pluginIds = info.pluginIds,
            externalDependencies = info.externalDependencies,
            customPlugins = customModuleTypes,
        )
    }

    override fun getConfigurations(projectPath: ProjectPath): List<GradleProjectConfiguration> =
        infoByPath[projectPath]?.configurations ?: emptyList()
}
