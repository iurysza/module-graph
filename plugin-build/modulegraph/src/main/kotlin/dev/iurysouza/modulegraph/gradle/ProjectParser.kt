package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.Theme
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

internal fun Project.parseProjectStructure(
    excludedConfigurations: String?,
    excludedModules: String?,
    theme: Theme,
): HashMap<Dependency, List<Dependency>> {
    val dependencies = hashMapOf<Dependency, List<Dependency>>()

    val configExclusionPattern = excludedConfigurations?.let { ExclusionStrategy.Configuration(it) }
    val projectExclusionPattern = excludedModules?.let { ExclusionStrategy.Project(it) }

    val customPlugins = if (theme is Theme.BASE) {
        theme.customPluginsColors
    } else {
        emptyList()
    }
    project.allprojects
        .asSequence()
        .filterNot { projectExclusionPattern.matches(it.path) }
        .forEach { sourceProject ->
            val sourceDependency = Dependency(
                path = sourceProject.path,
                plugin = sourceProject.identifyPlugin(customPlugins),
            )
            sourceProject.configurations.forEach { config ->
                config.dependencies
                    .withType(ProjectDependency::class.java)
                    .asSequence()
                    .map { it.dependencyProject }
                    .filterNot { configExclusionPattern.matches(config.name) }
                    .filterNot { projectExclusionPattern.matches(it.path) }
                    .forEach { targetProject ->
                        dependencies[sourceDependency] = dependencies.getOrDefault(sourceDependency, emptyList())
                            .plus(
                                Dependency(
                                    path = targetProject.path,
                                    configName = config.name,
                                    plugin = targetProject.identifyPlugin(customPlugins),
                                ),
                            )
                    }
            }
        }
    return dependencies
}
