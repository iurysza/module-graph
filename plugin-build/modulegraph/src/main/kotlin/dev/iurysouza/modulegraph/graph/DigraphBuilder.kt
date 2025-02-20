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
        verifySufficientGraph(graphResult, config.strictMode)

        return graphModel.flatMap { (source, targetList) ->
            when (config.linkText) {
                LinkText.NONE -> targetList.distinctBy { it.path }
                else -> targetList
            }.mapNotNull { target ->
                buildModel(config, source, target)
            }
        }.also { result ->
            throwIfNothingMatches(result, config.focusedModulesRegex, config.strictMode)
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

    private fun throwIfNothingMatches(modelList: List<DigraphModel>, regex: String?, strictMode: Boolean) {
        val errorMsg = """
                    |
                    |No modules were found matching the pattern: $regex
                    |This pattern was configured through the `focusedModulesRegex` property.
                    |Please verify that:
                    |1. The regex pattern is correct and matches your intended modules
                    |2. The modules you want to focus on exist in your project
                    |3. The modules are not being excluded by other configuration settings
            """.trimMargin()
        if (strictMode) {
            require(modelList.isNotEmpty()) { errorMsg }
        } else {
            println(errorMsg)
        }
    }

    private fun verifySufficientGraph(graphResult: GraphParseResult, strictMode: Boolean) {
        val graphModel = graphResult.graph
        val config = graphResult.config

        val dependencies = graphModel.values.flatten().distinctBy { it.path }.size
        val errorMsg = """
                    |
                    |Unable to generate dependency graph
                    |A minimum of two connected modules is required to create a meaningful graph.
                    |Please verify your configuration settings are not overly restrictive:
                    |
                    |Current configuration:
                    |$config
                    |
                    |Try adjusting the exclusion/inclusion patterns if needed.
            """.trimMargin()

        if (strictMode) {
            require(graphModel.keys.size > 1 || dependencies > 0) { errorMsg }
        } else {
            if (graphModel.keys.size <= 1 && dependencies == 0) {
                println(errorMsg)
            }
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
