package dev.iurysouza.modulegraph.graph

internal fun String.getProjectName(showFullPath: Boolean): String {
    return if (showFullPath) {
        this
    } else {
        this.split(":").last { it.isNotBlank() }
    }
}
internal fun Regex.isRegexFilterSet() = toString() != ".*"
