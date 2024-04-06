package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.Dependency
import dev.iurysouza.modulegraph.GraphOptions

internal object DigraphBuilder {
    fun build(
        graphModel: Map<String, List<Dependency>>,
        graphOptions: GraphOptions,
    ): List<DigraphModel> {
        throwIfSingleProject(graphModel)

        return graphModel.flatMap { (source, targetList) ->
            targetList.mapNotNull { target ->
                buildModel(graphOptions, source, target)
            }
        }.also { result ->
            throwIfNothingMatches(result, graphOptions.pattern)
        }
    }

    private fun buildModel(
        graphOptions: GraphOptions,
        sourceFullName: String,
        target: Dependency? = null,
    ): DigraphModel? {
        val pattern = graphOptions.pattern
        val showFullPath = graphOptions.showFullPath
        val targetFullName = target?.targetProjectPath

        val (sourceMatches, targetMatches) = when {
            pattern == null -> true to true
            else -> sourceFullName.matches(pattern) to (targetFullName?.matches(pattern) ?: false)
        }
        val regexFilterSet = pattern != null
        val shouldNotAddToGraph = sourceFullName == targetFullName || (!sourceMatches && !targetMatches)

        return when {
            shouldNotAddToGraph -> null
            else -> DigraphModel(
                source = ModuleNode(
                    name = sourceFullName.getProjectName(showFullPath),
                    fullName = sourceFullName,
                    isFocused = sourceMatches && regexFilterSet,
                    config = ModuleConfig.none(),
                    parent = sourceFullName.getParent(),
                ),
                target = ModuleNode(
                    name = targetFullName!!.getProjectName(showFullPath),
                    fullName = targetFullName,
                    isFocused = targetMatches && regexFilterSet,
                    config = ModuleConfig(target.configName),
                    parent = targetFullName.getParent(),
                ),
            )
        }
    }

    private fun throwIfNothingMatches(modelList: List<DigraphModel>, regex: Regex?) {
        require(modelList.isNotEmpty()) {
            """
                    |No modules match the specified pattern: $regex
                    |This was set via the `focusedNodesPattern` property.
            """.trimMargin()
        }
    }

    private fun throwIfSingleProject(graphModel: Map<String, List<Dependency>>) {
        val dependencies = graphModel.values.flatten().distinctBy { it.targetProjectPath }.size
        require(graphModel.keys.size > 1 || dependencies > 0) {
            """
                    |The project must have at least two modules to generate a graph.
            """.trimMargin()
        }
    }

    /**
     * Eg: ":app:module" -> "app"
     */
    private fun String.getParent(): String {
        val groups = split(":")
        if (groups.size == 1) return ""
        return groups
            .takeLast(2)
            .take(1)
            .joinToString("")
    }
}
