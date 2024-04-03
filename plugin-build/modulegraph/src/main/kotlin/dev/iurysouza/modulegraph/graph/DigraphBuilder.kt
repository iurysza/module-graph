package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.Dependency
import dev.iurysouza.modulegraph.GraphOptions

internal object DigraphBuilder {
    fun build(
        dependencies: Map<String, List<Dependency>>,
        graphOptions: GraphOptions,
    ): List<DigraphModel> = dependencies.flatMap { (source, targetList) ->
        targetList.mapNotNull { target ->
            buildModel(graphOptions, source, target)
        }
    }.also {
        require(it.isNotEmpty()) {
            """
            No modules match the specified pattern: ${graphOptions.pattern}
            This was set via the `focusedNodesPattern` property.
            """
        }
    }

    private fun buildModel(
        graphOptions: GraphOptions,
        sourceFullName: String,
        target: Dependency? = null,
    ): DigraphModel? {
        val (_, _, _, pattern, showFullPath) = graphOptions
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
                    parent = sourceFullName.getParent()
                ),
                target = ModuleNode(
                    name = targetFullName!!.getProjectName(showFullPath),
                    fullName = targetFullName,
                    isFocused = targetMatches && regexFilterSet,
                    config = ModuleConfig(target.configName),
                    parent = targetFullName.getParent()
                )
            )
        }
    }

    /**
     * Eg: ":app:module" -> "app"
     */
    private fun String.getParent(): String = split(":")
        .takeLast(2)
        .take(1)
        .joinToString("")
}

