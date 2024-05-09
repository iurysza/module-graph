package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.gradle.Module
import dev.iurysouza.modulegraph.model.GraphParseResult
import dev.iurysouza.modulegraph.model.SingleGraphConfig
import dev.iurysouza.modulegraph.model.alias.ProjectGraph

internal object DigraphBuilder {
    fun build(
        graphResult: GraphParseResult,
    ): List<DigraphModel> {
        val graphModel = graphResult.graph
        val config = graphResult.config
        throwIfSingleProject(graphModel)

        return graphModel.flatMap { (source, targetList) ->
            when (config.linkText) {
                LinkText.NONE -> targetList.distinctBy { it.path }
                else -> targetList
            }.mapNotNull { target ->
                buildModel(config, source, target)
            }
        }.also { result ->
            throwIfNothingMatches(result, config.focusedModulesRegex)
        }
    }

    private fun buildModel(
        config: SingleGraphConfig,
        source: Module,
        target: Module? = null,
    ): DigraphModel? {
        val focusedModulesPattern = config.focusedModulesRegex
        val focusedModulesRegex = focusedModulesPattern?.let { Regex(it) }
        val showFullPath = config.showFullPath
        val targetFullName = target?.path
        val sourceFullName = source.path
        val (sourceMatches, targetMatches) = when {
            focusedModulesRegex == null -> true to true
            else -> sourceFullName.matches(focusedModulesRegex) to (targetFullName?.matches(focusedModulesRegex) ?: false)
        }
        val regexFilterSet = focusedModulesRegex != null
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

    private fun throwIfNothingMatches(modelList: List<DigraphModel>, regex: String?) {
        require(modelList.isNotEmpty()) {
            """
                    |No modules match the specified pattern: $regex
                    |This was set via the `focusedModulesRegex` property.
            """.trimMargin()
        }
    }

    private fun throwIfSingleProject(graphModel: ProjectGraph) {
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
