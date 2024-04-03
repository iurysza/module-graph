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

}
