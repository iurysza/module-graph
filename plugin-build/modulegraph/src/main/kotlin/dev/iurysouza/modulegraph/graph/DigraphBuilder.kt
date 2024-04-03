package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.Dependency

internal object DigraphBuilder {
    fun build(
        input: DigraphInput,
    ): List<DigraphModel> = input.dependencies.flatMap { (source, targetList) ->
        targetList.mapNotNull { target ->
            buildModel(input, source, target)
        }
    }.also {
        require(it.isNotEmpty()) {
            """
            No modules match the specified pattern: ${input.pattern}
            This was set via the `focusedNodesPattern` property.
            """
        }
    }

    private fun buildModel(
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
                    config = ModuleConfig.none(),
                    parent = getParent(sourceFullName)
                ),
                target = ModuleNode(
                    name = targetFullName!!.getProjectName(showFullPath),
                    fullName = targetFullName,
                    isFocused = targetMatches && regexFilterSet,
                    config = ModuleConfig(target.configName),
                    parent = getParent(targetFullName)
                )
            )
        }
    }

    /**
     * Eg: ":app:module" -> "app"
     */
    private fun getParent(
        sourceFullName: String,
    ): String = sourceFullName
        .split(":")
        .takeLast(2)
        .take(1)
        .joinToString("")
}

internal fun Regex.isRegexFilterSet() = toString() != ".*"

