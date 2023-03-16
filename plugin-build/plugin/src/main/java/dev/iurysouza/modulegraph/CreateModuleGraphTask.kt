package dev.iurysouza.modulegraph

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

abstract class CreateModuleGraphTask : DefaultTask() {

    @get:Input
    @get:Option(option = "readmeFile", description = "The readme file to be used as input")
    abstract val readmeFile: Property<File>

    @get:Input
    @get:Option(option = "heading", description = "The heading where the graph will be appended")
    abstract val heading: Property<String>

    init {
        group = "Reporting"
        description = "Creates a mermaid dependency graph for the project"
        try {
            execute()
        } catch (e: Exception) {
            logger.log(LogLevel.ERROR, e.message, e)
        }
    }

    private fun execute() {
        doLast {
            val (dependencies, sortedProjects) = project.parseProjectStructure()
            val mermaidGraph = buildMermaidGraph(sortedProjects, dependencies)
            appendMermaidGraphToReadme(mermaidGraph, heading.get(), readmeFile.get())
        }
    }
}
