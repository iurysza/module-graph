package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.Theme
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

internal fun Project.parseProjectStructure(
    excludedConfigurations: String? = null,
    excludedModules: String? = null,
    theme: Theme?,
): HashMap<Dependency, List<Dependency>> {
    val dependencies = hashMapOf<Dependency, List<Dependency>>()
    val exclusionStrategy = buildSet {
        excludedConfigurations?.let { add(ExclusionStrategy.Configuration(it)) }
        excludedModules?.let { add(ExclusionStrategy.Project(it)) }
    }
    val customPlugins = if (theme is Theme.BASE) {
        theme.customPluginsColors
    } else {
        emptyList()
    }
    project.allprojects
        .asSequence()
        .filter { it.matches(exclusionStrategy) }
        .forEach { sourceProject ->
            val sourceDependency = Dependency(
                path = sourceProject.path,
                plugin = sourceProject.identifyPlugin(customPlugins),
            )
            sourceProject.configurations
                .asSequence()
                .forEach { config ->
                    config.dependencies
                        .withType(ProjectDependency::class.java)
                        .map { it.dependencyProject }
                        .filter { it.matches(exclusionStrategy) }
                        .filter { config.matches(exclusionStrategy) }
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
