package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme
import dev.iurysouza.modulegraph.model.GraphConfig
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * The ModuleGraphExtension is used for configuring the module graph generation in a project.
 *
 * There are 2 ways to specify the config for a graph:
 * - The primary graph, where the config is applied directly to the properties in this extension.
 *   This is needed for convenience when only a single graph is needed,
 *   and for backwards compatibility.
 *
 * - Additional graphs, stored in [graphConfigs].
 *   Consumers can add a [graph] block for every graph required,
 *   which will add a config to [graphConfigs].
 *   There is no limit on how many additional graphs can be added.
 *   It is possible to use this plugin without setting up the primary graph -
 *   all configs can be set up as Additional graphs.
 */
open class ModuleGraphExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    /**
     * The mermaid theme to be used for the generated graph.
     * This is an optional input. Defaults to [Theme.NEUTRAL].
     */
    val theme: Property<Theme> = objects.property(Theme::class.java)

    /**
     * The orientation of the flowchart in the generated graph.
     * This is an optional input. Defaults to [Orientation.LEFT_TO_RIGHT].
     */
    val orientation: Property<Orientation> = objects.property(Orientation::class.java)

    /**
     * The Pattern (Regex) to match nodes in the graph (project names) that should be highlighted and focused.
     * The graph will only show nodes that either point to the matched nodes or originate from them.
     *
     * The Value needs to be a string in regex format.
     */
    val focusedModulesRegex: Property<String> = objects.property(String::class.java)

    /**
     * The path of the readme file where the graph will be appended.
     * This is a required input.
     */
    val readmePath: Property<String> = objects.property(String::class.java)

    /**
     * The heading under which the graph will be appended in the readme file.
     * This is a required input.
     */
    val heading: Property<String> = objects.property(String::class.java)

    /**
     * Whether to add information as text on links in the graph.
     * This is an optional input. Defaults to [LinkText.NONE].
     */
    val linkText: Property<LinkText> = objects.property(LinkText::class.java)

    /**
     * Whether to use custom styling for module nodes based on the plugin type.
     */
    val setStyleByModuleType: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * A Regex to match configurations that should be ignored.
     */
    val excludedConfigurationsRegex: Property<String> = objects.property(String::class.java)

    /**
     * A Regex to match modules that should be excluded from the graph.
     */
    val excludedModulesRegex: Property<String> = objects.property(String::class.java)

    /**
     * A Regex to match modules that should be used as root modules.
     * If this value is supplied,
     * the generated graph will only include dependencies (direct and transitive) of root modules.
     */
    val rootModulesRegex: Property<String> = objects.property(String::class.java)

    /**
     * Whether to show the full path of the module in the graph.
     * Use this if you have modules with the same name in different folders.
     * Note: when using this option, the graph generated won't use the subgraph feature mermaid provides.
     */
    val showFullPath: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * A list of additional graph configs to generate graphs for.
     */
    val graphConfigs: ListProperty<GraphConfig> =
        objects.listProperty(GraphConfig::class.java)

    /**
     * Creates a new [GraphConfig] based on the configuration block,
     * and adds it to [graphConfigs].
     * This function provides a DSL for adding graphs.
     */
    fun graph(
        readmePath: String,
        heading: String,
        setupConfig: GraphConfig.Builder.() -> Unit,
    ) {
        val configBuilder = GraphConfig.Builder(readmePath, heading)
        configBuilder.setupConfig()
        val newConfig = configBuilder.build()
        val existingList = graphConfigs.get()
        val newList = existingList + newConfig
        graphConfigs.set(newList)
    }
}
