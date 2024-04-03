package dev.iurysouza.modulegraph.gradle.graph

internal fun String.getProjectName(showFullPath: Boolean): String {
    return if (showFullPath) {
        this
    } else {
        this.split(":").last { it.isNotBlank() }
    }
}
