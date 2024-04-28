package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.GraphOptions
import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.gradle.Module

internal object DigraphBuilder {
    fun build(
        graphModel: Map<Module, List<Module>>,
        graphOptions: GraphOptions,
    ): List<DigraphModel> {
        throwIfSingleProject(graphModel)

        return graphModel.flatMap { (source, targetList) ->
            when (graphOptions.linkText) {
                LinkText.NONE -> targetList.distinctBy { it.path }
                else -> targetList
            }.mapNotNull { target ->
                buildModel(graphOptions, source, target)
            }
        }.also { result ->
            throwIfNothingMatches(result, graphOptions.focusedNodesRegex)
        }
    }

    private fun buildModel(
        graphOptions: GraphOptions,
        source: Module,
        target: Module? = null,
    ): DigraphModel? {
        val pattern = graphOptions.focusedNodesRegex
        val showFullPath = graphOptions.showFullPath
        val targetFullName = target?.path
        val sourceFullName = source.path
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
                    type = source.type,
                    parent = sourceFullName.getParent(),
                ),
                target = ModuleNode(
                    name = targetFullName!!.getProjectName(showFullPath),
                    fullName = targetFullName,
                    isFocused = targetMatches && regexFilterSet,
                    config = target.configName?.let { ModuleConfig(it) } ?: ModuleConfig.none(),
                    type = target.type,
                    parent = targetFullName.getParent(),
                ),
            )
        }
    }

    private fun throwIfNothingMatches(modelList: List<DigraphModel>, regex: Regex?) {
        require(modelList.isNotEmpty()) {
            """
                    |No modules match the specified pattern: $regex
                    |This was set via the `focusedModulesRegex` property.
            """.trimMargin()
        }
    }

    private fun throwIfSingleProject(graphModel: Map<Module, List<Module>>) {
        val dependencies = graphModel.values.flatten().distinctBy { it.path }.size
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
            .dropLast(1)
            .joinToString(":")
    }
}
