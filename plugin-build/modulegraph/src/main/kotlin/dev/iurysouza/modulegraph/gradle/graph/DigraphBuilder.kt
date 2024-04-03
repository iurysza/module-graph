package dev.iurysouza.modulegraph.gradle.graph

import dev.iurysouza.modulegraph.Dependency

internal object DigraphBuilder {
    fun build(
        input: DigraphInput,
    ): List<DigraphModel> = input.dependencies.flatMap { (source, targetList) ->
        val sourceNode = buildNode(source, input)
        targetList.mapNotNull { target ->
            val targetNode = buildNode(
                sourceFullName = source,
                input = input,
                target = target,
            )
            if (targetNode != null && sourceNode != null) {
                DigraphModel(sourceNode, targetNode)
            } else {
                null
            }
        }
    }

    private fun buildNode(
        sourceFullName: String,
        input: DigraphInput,
        target: Dependency? = null,
    ): ModuleNode? {
        val (pattern, _, showFullPath) = input
        val targetFullName = target?.targetProjectPath

        val sourceMatches = sourceFullName.matches(pattern)
        val targetMatches = targetFullName?.matches(pattern) ?: false
        val regexFilterSet = pattern.isRegexFilterSet()

        val shouldNotAddToGraph = sourceFullName == targetFullName || (!sourceMatches && !targetMatches)

        return when {
            shouldNotAddToGraph -> null
            target != null -> ModuleNode(
                name = targetFullName?.getProjectName(showFullPath) ?: "",
                fullName = target.targetProjectPath,
                isFocused = targetMatches && regexFilterSet,
                config = ModuleConfig(target.configName)
            )
            else -> ModuleNode(
                name = sourceFullName.getProjectName(showFullPath),
                fullName = sourceFullName,
                isFocused = regexFilterSet,
                config = ModuleConfig.none()
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
