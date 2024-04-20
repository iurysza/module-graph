package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.*
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
    @get:Option(option = "focusedNodesPattern", description = "A Regex to match nodes that should be focused.")
    @get:Optional
    abstract val focusedNodesPattern: Property<String>

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
    @get:Option(
        option = "excludedConfigurationsRegex",
        description = "A Regex to match configurations that should removed",
    )
    @get:Optional
    abstract val excludedConfigurationsRegex: Property<String>

    @get:Input
    @get:Option(option = "excludedModulesRegex", description = "A Regex to match nodes that should removed")
    @get:Optional
    abstract val excludedModulesRegex: Property<String>

    @get:Input
    @get:Option(option = "setStyleByModuleType", description = "Whether to customize the node by the plugin type")
    @get:Optional
    abstract val setStyleByModuleType: Property<Boolean>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    @get:Option(option = "graphModel", description = "The project graph model")
    internal abstract val graphModel: MapProperty<Module, List<Module>>

    init {
        group = "Reporting"
        description = "Creates a mermaid dependency graph for the project"
    }

    @TaskAction
    fun execute() {
        runCatching {
            val graphOptions = GraphOptions(
                theme = theme.getOrElse(Theme.NEUTRAL),
                orientation = orientation.getOrElse(Orientation.LEFT_TO_RIGHT),
                pattern = focusedNodesPattern.orNull?.let { Regex(it) },
                showFullPath = showFullPath.getOrElse(false),
                linkText = linkText.getOrElse(LinkText.NONE),
                setStyleByModuleType = setStyleByModuleType.getOrElse(false),
            )
            val mermaidGraph = Mermaid.generateGraph(graphModel.get(), graphOptions)
            ReadmeWriter.appendOrOverwriteGraph(
                mermaidGraph = mermaidGraph,
                readMeSection = heading.get(),
                readmeFile = outputFile.get().asFile,
                logger = logger,
            )
        }.onFailure {
            logger.log(LogLevel.ERROR, it.message, it)
            throw it
        }
    }
}
