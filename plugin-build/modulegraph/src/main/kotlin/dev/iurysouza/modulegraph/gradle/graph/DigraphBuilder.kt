package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.Dependency

internal object DigraphBuilder {
    fun build(
        input: DigraphInput,
    ): List<DigraphModel> = input.dependencies.flatMap { (source, targetList) ->
        targetList.mapNotNull { target ->
            buildDigraph(input, source, target)
        }
    }

    private fun buildDigraph(
        input: DigraphInput,
        sourceFullName: String,
        target: Dependency? = null,
    ): DigraphModel? {
        val (pattern, _, showFullPath) = input
        val targetFullName = target?.targetProjectPath

        val sourceMatches = sourceFullName.matches(pattern)
        val targetMatches = targetFullName?.matches(pattern) ?: false
        val regexFilterSet = pattern.isRegexFilterSet()
        val shouldNotAddToGraph = sourceFullName == targetFullName || (!sourceMatches && !targetMatches)

        return when {
            shouldNotAddToGraph -> null
            else -> DigraphModel(
                source = ModuleNode(
                    name = sourceFullName.getProjectName(showFullPath),
                    fullName = sourceFullName,
                    isFocused = sourceMatches && regexFilterSet,
                    config = ModuleConfig.none()
                ),
                target = ModuleNode(
                    name = targetFullName!!.getProjectName(showFullPath),
                    fullName = targetFullName,
                    isFocused = targetMatches && regexFilterSet,
                    config = ModuleConfig(target.configName)
                )
            )
        }
    }
}

private fun Regex.isRegexFilterSet() = toString() != ".*"

private fun String.getProjectName(showFullPath: Boolean): String {
    return if (showFullPath) {
        this
    } else {
        this.split(":").last { it.isNotBlank() }
    }
}
