package dev.iurysouza.modulegraph

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class CreateModuleGraphTask : DefaultTask() {

    @get:Input
    @get:Option(option = "readmePath", description = "The readme file path")
    abstract val readmePath: Property<String>

    @get:Input
    @get:Option(option = "theme", description = "The mermaid theme")
    @get:Optional
    abstract val theme: Property<Theme>

    @get:Input
    @get:Option(option = "heading", description = "The heading where the graph will be appended")
    abstract val heading: Property<String>

    @get:Input
    @get:Option(option = "dependencies", description = "The project dependencies")
    abstract val dependencies: MapProperty<String, List<String>>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        group = "Reporting"
        description = "Creates a mermaid dependency graph for the project"
    }

    @TaskAction
    fun execute() {
        try {
            val mermaidGraph = buildMermaidGraph(
                theme = theme.getOrElse(Theme.NEUTRAL),
                dependencies = dependencies.get()
            )
            appendMermaidGraphToReadme(mermaidGraph, heading.get(), outputFile.get().asFile, logger)
        } catch (e: Exception) {
            logger.log(LogLevel.ERROR, e.message, e)
        }
    }
}
