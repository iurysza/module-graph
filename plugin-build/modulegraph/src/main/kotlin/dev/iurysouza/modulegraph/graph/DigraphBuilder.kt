package dev.iurysouza.modulegraph.graph

import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.gradle.Module
import dev.iurysouza.modulegraph.model.GraphConfig
import dev.iurysouza.modulegraph.model.GraphParseResult

internal object DigraphBuilder {
    fun build(
        graphResult: GraphParseResult,
    ): List<DigraphModel> {
        val graphModel = graphResult.graph
        val config = graphResult.config
        verifySufficientGraph(graphResult)

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
        config: GraphConfig,
        source: Module,
        target: Module? = null,
    ): DigraphModel? {
        val focusedModulesRegex = config.focusedModulesRegex?.let { Regex(it) }
        val showFullPath = config.showFullPath
        val targetFullName = target?.path
        val sourceFullName = source.path
        val (sourceMatches, targetMatches) = when {
            focusedModulesRegex == null -> true to true
            else -> {
                val match = targetFullName?.matches(focusedModulesRegex) ?: false
                sourceFullName.matches(focusedModulesRegex) to match
            }
        }
        val isFocusedModulesRegexSet = focusedModulesRegex != null
        val shouldNotAddToGraph =
            sourceFullName == targetFullName || (!sourceMatches && !targetMatches)

        return when {
            shouldNotAddToGraph -> null
            else -> DigraphModel(
                source = ModuleNode(
                    name = sourceFullName.getProjectName(showFullPath),
                    fullName = sourceFullName,
                    isFocused = sourceMatches && isFocusedModulesRegexSet,
                    config = ModuleConfig.none(),
                    type = source.type,
                    parent = sourceFullName.getParent(),
                ),
                target = ModuleNode(
                    name = targetFullName!!.getProjectName(showFullPath),
                    fullName = targetFullName,
                    isFocused = targetMatches && isFocusedModulesRegexSet,
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

    private fun verifySufficientGraph(graphResult: GraphParseResult) {
        val graphModel = graphResult.graph
        val config = graphResult.config

        val dependencies = graphModel.values.flatten().distinctBy { it.path }.size
        require(graphModel.keys.size > 1 || dependencies > 0) {
            """
                    |The project must have at least two modules to generate a graph.
                    |It may be that the config is too restrictive: $config
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
