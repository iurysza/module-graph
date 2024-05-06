package dev.iurysouza.modulegraph.gradle

internal fun RegexMatcher?.matches(
    name: String,
) = this?.pattern
    ?.toRegex()
    ?.matches(name) ?: false

internal data class RegexMatcher(val pattern: String)
