package dev.iurysouza.modulegraph

import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.logging.Logger

/**
 * Generates a Mermaid graph for module dependencies.
 * - Groups: Folders containing modules
 * - Subgraphs: Grouped modules in the graph
 * - Source: Module with dependencies
 * - Target: Dependent module
 * - Digraph: Represents the dependencies between two modules: source -> target
 */
internal fun buildMermaidGraph(
    theme: Theme,
    orientation: Orientation,
    linkText: LinkText,
    pattern: Regex,
    dependencies: MutableMap<String, List<Dependency>>,
    showFullPath: Boolean,
): String {
    val projectPaths = dependencies
        .entries
        .asSequence()
        .flatMap { (source, target) -> target.map { it.targetProjectPath }.plus(source) }
        .sorted()

    val mostMeaningfulGroups: List<String> = projectPaths
        .map { it.split(":").takeLast(2).take(1) }
        .distinct()
        .flatten()
        .toList()

    val projectNames = projectPaths
        .map { listOf(it.split(":").takeLast(2)) }
        .distinct()
        .flatten()
        .toList()

    val subgraphs = buildSubgraph(pattern, showFullPath, mostMeaningfulGroups, projectNames)
    val (digraph, focusList) = buildDigraph(pattern, dependencies, showFullPath, linkText)
    val highlightedNodes = highlightNodes(focusList, pattern, theme)

    return """
${createConfig(theme)}

graph ${orientation.value}
$subgraphs$digraph
$highlightedNodes""".trimIndent()
}

private fun highlightNodes(focusList: Set<String>, pattern: Regex, theme: Theme): String {
    val focusColor = when (theme) {
        is Theme.BASE -> theme.focusColor
        else -> DefaultFocusColor
    }
    val focusClassName = "focus"
    return """${
        if (focusList.isNotEmpty() && pattern.toString() != ".*") {
            buildString {
                append("\n")
                append("classDef $focusClassName fill:$focusColor,stroke:#fff,stroke-width:2px,color:#fff;")
                append("\n")
                focusList.forEach { projectName ->
                    append("class $projectName $focusClassName\n")
                }
            }
        } else {
            ""
        }
    }
"""
}

private fun buildSubgraph(
    pattern: Regex,
    showFullPath: Boolean,
    mostMeaningfulGroups: List<String>,
    projectNames: List<List<String>>,
) = if (showFullPath) {
    ""
} else {
    mostMeaningfulGroups.filter { it.matches(pattern) }.joinToString("\n") { group ->
        createSubgraph(group, projectNames)
    }.plus("\n")
}

private fun LinkText.toLinkString(configName: String?): String = when (this) {
    LinkText.CONFIGURATION -> "-- $configName -->"
    LinkText.NONE -> "-->"
}

private fun String.getProjectName(showFullPath: Boolean): String {
    return if (showFullPath) {
        this
    } else {
        this.split(":").last { it.isNotBlank() }
    }
}

private fun buildDigraph(
    pattern: Regex,
    dependencies: Map<String, List<Dependency>>,
    showFullPath: Boolean,
    linkText: LinkText,
): Pair<String, Set<String>> {
    val focusedProjects = mutableSetOf<String>()
    return dependencies.filterKeys { it != ":" }.flatMap { entry ->
        val sourceName = entry.key.getProjectName(showFullPath)
        entry.value.mapNotNull { target ->
            val targetName = target.targetProjectPath.getProjectName(showFullPath)
            val (shouldAddToGraph, matching) = shouldAddToGraph(
                pattern = pattern,
                source = entry.key,
                target = target.targetProjectPath,
                showFullPath = showFullPath
            )
            if (shouldAddToGraph) {
                focusedProjects.addAll(matching)
                "  $sourceName ${linkText.toLinkString(target.configName)} $targetName"
            } else {
                null
            }
        }
    }
        .distinct()
        .joinToString(separator = "\n") to focusedProjects
}

private fun shouldAddToGraph(
    pattern: Regex,
    source: String,
    target: String,
    showFullPath: Boolean,
): Pair<Boolean, List<String>> {
    val sourceMatchesPattern = source.split(":").any { it.matches(pattern) }
    val targetMatchesPattern = target.split(":").any { it.matches(pattern) }
    val sourceName = source.getProjectName(showFullPath)
    val targetName = target.getProjectName(showFullPath)
    return Pair(
        source != target && (sourceMatchesPattern || targetMatchesPattern),
        when {
            sourceMatchesPattern && targetMatchesPattern -> listOf(sourceName, targetName)
            sourceMatchesPattern -> listOf(sourceName)
            targetMatchesPattern -> listOf(targetName)
            else -> listOf()
        }
    )
}

private fun createConfig(theme: Theme): String = """
%%{
  init: {
    'theme': '${theme.name}'${
    if (theme is Theme.BASE && theme.themeVariables.isNotEmpty()) ",\n\t'themeVariables': ${
        Json.encodeToString(theme.themeVariables).trimIndent()
    }" else ""
}
  }
}%%
""".trimIndent()

// Generate a Mermaid subgraph for the specified group and list of modules
fun createSubgraph(group: String, modules: List<List<String>>): String {
    // Extract module names for the current group
    val moduleNames = modules.asSequence()
        .map { it.takeLast(2) } // group by the most relevant part of the path
        .filter { it.all { it.length > 2 } }
        .filter { it.contains(group) }
        .map { it.last() } // take the actual module name
        .joinToString("\n    ") { moduleName -> moduleName }

    if (moduleNames.isBlank()) {
        return ""
    }
    return "  subgraph $group\n    $moduleNames\n  end"
}

internal fun appendMermaidGraphToReadme(
    mermaidGraph: String,
    readMeSection: String,
    readmeFile: File,
    logger: Logger,
) {
    if (!readmeFile.exists()) {
        readmeFile.createNewFile()
        logger.warn(
            """
                The specified README file was not found.
                A new file has been created at: ${readmeFile.path}
            """.trimMargin()
        )
    }
    val readmeLines = readmeFile.readLines().toMutableList()
    val sectionStartIndex = findPredefinedSection(readmeLines, readMeSection)
    val sectionEndIndex = findNextSectionStart(readmeLines, sectionStartIndex)

    readmeLines.subList(sectionStartIndex + 1, sectionEndIndex).clear()
    if (sectionStartIndex == -1) {
        readmeLines.add(0, "$readMeSection\n\n```mermaid\n$mermaidGraph\n```")
    } else {
        readmeLines.add(sectionStartIndex + 1, "\n```mermaid\n$mermaidGraph\n```")
    }
    readmeFile.writeText(readmeLines.joinToString("\n"))
    logger.debug("Module graph added to ${readmeFile.path} under the $readMeSection section")
}

private fun findPredefinedSection(readmeLines: List<String>, section: String): Int {
    return readmeLines.indexOfFirst { it.startsWith(section) }
}

private fun findNextSectionStart(readmeLines: List<String>, startIndex: Int): Int {
    return readmeLines.drop(startIndex + 1).indexOfFirst { it.startsWith("#") }.let {
        if (it != -1) it + startIndex + 1 else readmeLines.size
    }
}
