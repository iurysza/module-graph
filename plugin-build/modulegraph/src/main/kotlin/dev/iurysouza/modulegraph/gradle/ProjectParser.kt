package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.Dependency
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency

internal fun Project.parseProjectStructure(
    excludedConfigurations: String? = null,
    excludedModules: String? = null,
): HashMap<String, List<Dependency>> {
    val dependencies = hashMapOf<String, List<Dependency>>()
    val exclusionStrategy = buildSet {
        excludedConfigurations?.let { add(ExclusionStrategy.Configuration(it)) }
        excludedModules?.let { add(ExclusionStrategy.Project(it)) }
    }
    project.allprojects
        .asSequence()
        .filter { it.matches(exclusionStrategy) }
        .forEach { sourceProject ->
            sourceProject.configurations
                .asSequence()
                .forEach { config ->
                    config.dependencies.withType(ProjectDependency::class.java)
                        .map { it.dependencyProject }
                        .filter { it.matches(exclusionStrategy) }
                        .filter { config.matches(exclusionStrategy) }
                        .forEach { targetProject ->
                            dependencies[sourceProject.path] =
                                dependencies.getOrDefault(sourceProject.path, emptyList())
                                    .plus(Dependency(targetProject.path, config.name))
                        }
                }
        }
    return dependencies
}

private fun Configuration.matches(
    exclusionStrategies: Set<ExclusionStrategy>,
) = exclusionStrategies.filterIsInstance<ExclusionStrategy.Configuration>().none { it.pattern.toRegex().matches(name) }

private fun Project.matches(
    exclusionStrategies: Set<ExclusionStrategy>,
) = exclusionStrategies.filterIsInstance<ExclusionStrategy.Project>().none { it.pattern.toRegex().matches(name) }

sealed class ExclusionStrategy(open val pattern: String) {
    data class Project(override val pattern: String) : ExclusionStrategy(pattern)
    data class Configuration(override val pattern: String) : ExclusionStrategy(pattern)
}
