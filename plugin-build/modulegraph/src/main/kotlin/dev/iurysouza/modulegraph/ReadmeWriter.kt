package dev.iurysouza.modulegraph

import java.io.File
import org.gradle.api.logging.Logger

internal object ReadmeWriter {
    fun appendOrOverwriteGraph(
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
                """.trimMargin(),
            )
        }
        val readmeLines = readmeFile.readLines().toMutableList()
        val sectionStartIndex = findPredefinedSection(readmeLines, readMeSection)
        val sectionEndIndex = findNextSectionStart(readmeLines, sectionStartIndex)

        readmeLines.subList(sectionStartIndex + 1, sectionEndIndex).clear()
        if (sectionStartIndex == -1) {
            readmeLines.add(0, "$readMeSection\n\n$mermaidGraph")
        } else {
            readmeLines.add(sectionStartIndex + 1, "\n$mermaidGraph")
        }
        readmeFile.writeText(readmeLines.joinToString("\n"))
        logger.debug("Module graph added to ${readmeFile.path} under the $readMeSection section")
    }

    private fun findPredefinedSection(readmeLines: List<String>, section: String): Int {
        val index = readmeLines.indexOfFirst { it.startsWith(section) }
        return index
    }

    private fun findNextSectionStart(readmeLines: List<String>, startIndex: Int): Int {
        val relativeIndex = readmeLines.drop(startIndex + 1).indexOfFirst { it.startsWith("#") }
        return if (relativeIndex == -1) readmeLines.size else relativeIndex + startIndex + 1
    }
}
