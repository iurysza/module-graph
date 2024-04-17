package dev.iurysouza.modulegraph.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

internal fun Configuration.matches(
    exclusionStrategies: Set<ExclusionStrategy>,
) = exclusionStrategies
    .filterIsInstance<ExclusionStrategy.Configuration>()
    .none { it.pattern.toRegex().matches(name) }

internal fun Project.matches(
    exclusionStrategies: Set<ExclusionStrategy>,
) = exclusionStrategies
    .filterIsInstance<ExclusionStrategy.Project>()
    .none { it.pattern.toRegex().matches(name) }

internal sealed class ExclusionStrategy(open val pattern: String) {
    data class Project(override val pattern: String) : ExclusionStrategy(pattern)
    data class Configuration(override val pattern: String) : ExclusionStrategy(pattern)
}
