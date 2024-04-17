package dev.iurysouza.modulegraph.gradle

internal fun ExclusionStrategy?.matches(
    name: String,
) = this?.pattern
    ?.toRegex()
    ?.matches(name) ?: false

internal sealed class ExclusionStrategy(open val pattern: String) {
    data class Project(override val pattern: String) : ExclusionStrategy(pattern)
    data class Configuration(override val pattern: String) : ExclusionStrategy(pattern)
}
