package dev.iurysouza.modulegraph.gradle.graphparser.projectquerier

import dev.iurysouza.modulegraph.ModuleType
import dev.iurysouza.modulegraph.gradle.graphparser.model.GradleProjectConfiguration
import dev.iurysouza.modulegraph.gradle.graphparser.model.ProjectPath

/**
 * This interface provides data relating to Gradle projects identified by their [ProjectPath].
 * This creates an abstraction between the actual Gradle implementation.
 *
 * By having this as an interface with functions,
 * we can delay the look-up of the data until it is requested.
 * This helps us avoid infinite recursion, as might happen if we had to resolve all the data at once.
 */
internal interface ProjectQuerier {
    fun getProjectType(projectPath: ProjectPath, customModuleTypes: List<ModuleType>): ModuleType
    fun getConfigurations(projectPath: ProjectPath): List<GradleProjectConfiguration>
}
