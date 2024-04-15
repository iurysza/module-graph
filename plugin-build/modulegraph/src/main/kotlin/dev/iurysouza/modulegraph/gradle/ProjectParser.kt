package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.Dependency
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency

internal fun Project.parseProjectStructure(
    exclusionStrategy: Set<ExclusionStrategy> = emptySet(),
): HashMap<String, List<Dependency>> {
    val dependencies = hashMapOf<String, List<Dependency>>()
    project.allprojects
        .asSequence()
        .filter { project -> exclusionStrategy.projectMatches(project) }
        .forEach { sourceProject ->
            sourceProject.configurations
                .asSequence()
                .forEach { config ->
                    config.dependencies.withType(ProjectDependency::class.java)
                        .map { it.dependencyProject }
                        .filter { exclusionStrategy.projectMatches(it) }
                        .filter { exclusionStrategy.configurationMatches(config) }
                        .forEach { targetProject ->
                            dependencies[sourceProject.path] =
                                dependencies.getOrDefault(sourceProject.path, emptyList())
                                    .plus(Dependency(targetProject.path, config.name))
                        }
                }
        }
    return dependencies
}

private fun Set<ExclusionStrategy>.configurationMatches(
    configuration: Configuration,
) = filterIsInstance<ExclusionStrategy.Configuration>().none { it.pattern.toRegex().matches(configuration.name) }

private fun Set<ExclusionStrategy>.projectMatches(
    project: Project,
) = filterIsInstance<ExclusionStrategy.Project>().none { it.pattern.toRegex().matches(project.name) }


sealed class ExclusionStrategy(open val pattern: String) {
    data class Project(override val pattern: String) : ExclusionStrategy(pattern)
    data class Configuration(override val pattern: String) : ExclusionStrategy(pattern)
}
