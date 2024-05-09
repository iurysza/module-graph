package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.Mermaid
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.ReadmeWriter
import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.model.GraphParseResult
import dev.iurysouza.modulegraph.model.SingleGraphConfig
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
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
    @get:Option(
        option = "focusedModulesRegex",
        description = "A Regex to match modules that should be focused.",
    )
    @get:Optional
    abstract val focusedModulesRegex: Property<String>

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
    @get:Option(
        option = "linkText",
        description = "Whether to add information as text on links in graph",
    )
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
    @get:Option(
        option = "excludedModulesRegex",
        description = "A Regex to match modules that should removed",
    )
    @get:Optional
    abstract val excludedModulesRegex: Property<String>

    @get:Input
    @get:Option(option = "rootModulesRegex", description = "A Regex to match root modules")
    @get:Optional
    abstract val rootModulesRegex: Property<String>

    @get:Input
    @get:Option(
        option = "graphConfigs",
        description = "A list of configs, each of which will generate a separate graph",
    )
    @get:Optional
    abstract val graphConfigs: ListProperty<SingleGraphConfig>

    @get:Input
    @get:Option(
        option = "setStyleByModuleType",
        description = "Whether to customize the module by the plugin type",
    )
    @get:Optional
    abstract val setStyleByModuleType: Property<Boolean>

    // todo: do we need this?
//    @get:OutputFile
//    abstract val outputFile: RegularFileProperty

    @get:Input
    @get:Option(option = "graphModels", description = "The produced graph models")
    internal abstract val graphModels: ListProperty<GraphParseResult>

    init {
        group = "Reporting"
        description = "Creates a mermaid dependency graph for the project"
    }

    @TaskAction
    fun execute() {
        runCatching {
            val results =
                graphModels.orNull ?: error("Graph models have not been computed on time!")
            results.forEach { result ->
                val config = result.config
                val mermaidGraph = Mermaid.generateGraph(result)

                val readmeFile = project.layout.projectDirectory.file(config.readmePath)

                ReadmeWriter.appendOrOverwriteGraph(
                    mermaidGraph = mermaidGraph,
                    readMeSection = config.heading,
                    readmeFile = readmeFile.asFile,
                    logger = logger,
                )
            }
        }.onFailure {
            logger.log(LogLevel.ERROR, it.message, it)
            throw it
        }
    }
}
