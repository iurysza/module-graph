package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.Dependency
import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.appendMermaidGraphToReadme
import dev.iurysouza.modulegraph.buildMermaidGraph
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
    @get:Option(option = "orientation", description = "The flowchart orientation")
    @get:Optional
    abstract val orientation: Property<Orientation>

    @get:Input
    @get:Option(option = "showFullPath", description = "Whether to show the modules full path")
    @get:Optional
    abstract val showFullPath: Property<Boolean>

    @get:Input
    @get:Option(option = "heading", description = "The heading where the graph will be appended")
    abstract val heading: Property<String>

    @get:Input
    @get:Option(option = "linkText", description = "Whether to add information as text on links in graph")
    @get:Optional
    abstract val linkText: Property<LinkText>

    @get:Input
    @get:Option(option = "dependencies", description = "The project dependencies")
    internal abstract val dependencies: MapProperty<String, List<Dependency>>

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
                orientation = orientation.getOrElse(Orientation.LEFT_TO_RIGHT),
                linkText = linkText.getOrElse(LinkText.NONE),
                dependencies = dependencies.get(),
                showFullPath = showFullPath.getOrElse(false)
            )
            appendMermaidGraphToReadme(
                mermaidGraph = mermaidGraph,
                readMeSection = heading.get(),
                readmeFile = outputFile.get().asFile,
                logger = logger
            )
        } catch (e: Exception) {
            logger.log(LogLevel.ERROR, e.message, e)
        }
    }
}
