package dev.iurysouza.modulegraph.graph

internal fun String.getProjectName(
    showFullPath: Boolean,
): String = runCatching {
    if (showFullPath) {
        this
    } else {
        this.split(":").last { it.isNotBlank() }
    }
}.getOrElse { this }

internal fun StringBuilder.lineBreak() {
    append("\n")
}
