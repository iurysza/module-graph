package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.getModuleType
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

internal fun Project.parseProjectStructure(
    excludedConfigurations: String?,
    excludedModules: String?,
    theme: Theme,
): HashMap<Module, List<Module>> {
    val dependencies = hashMapOf<Module, List<Module>>()

    val configExclusionPattern = excludedConfigurations?.let { ExclusionStrategy.Configuration(it) }
    val projectExclusionPattern = excludedModules?.let { ExclusionStrategy.Project(it) }

    val customModuleTypes = when (theme) {
        is Theme.BASE -> theme.moduleTypes
        else -> emptyList()
    }
    project.allprojects
        .asSequence()
        .filterNot { projectExclusionPattern.matches(it.path) }
        .forEach { sourceProject ->
            val sourceModule = Module(
                path = sourceProject.path,
                type = sourceProject.getModuleType(customModuleTypes),
            )
            sourceProject.configurations.forEach { config ->
                config.dependencies
                    .withType(ProjectDependency::class.java)
                    .asSequence()
                    .map { it.dependencyProject }
                    .filterNot { configExclusionPattern.matches(config.name) }
                    .filterNot { projectExclusionPattern.matches(it.path) }
                    .forEach { targetProject ->
                        dependencies[sourceModule] = dependencies.getOrDefault(sourceModule, emptyList())
                            .plus(
                                Module(
                                    path = targetProject.path,
                                    configName = config.name,
                                    type = targetProject.getModuleType(customModuleTypes),
                                ),
                            )
                    }
            }
        }
    return dependencies
}
