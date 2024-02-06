package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.Dependency
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

internal fun Project.parseProjectStructure(excludeConfigurationNames: List<String>): HashMap<String, List<Dependency>> {
    val dependencies = hashMapOf<String, List<Dependency>>()
    project.allprojects.forEach { sourceProject ->
        sourceProject.configurations.forEach { config ->
            config.dependencies.withType(ProjectDependency::class.java)
                .map { it.dependencyProject }
                .forEach { targetProject ->
                    if(!excludeConfigurationNames.contains(config.name)) {
                        dependencies[sourceProject.path] =
                            dependencies.getOrDefault(sourceProject.path, emptyList())
                                .plus(Dependency(targetProject.path, config.name))
                    }
                }
        }
    }
    return dependencies
}
