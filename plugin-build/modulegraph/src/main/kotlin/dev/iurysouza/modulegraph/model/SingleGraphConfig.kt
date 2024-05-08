package dev.iurysouza.modulegraph.model

import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme
import java.io.Serializable

/**
 * The config for a single graph to be made.
 */
data class SingleGraphConfig(
    /**
     * The path of the readme file where the graph will be appended.
     */
    val readmePath: String,

    /**
     * The heading under which the graph will be appended in the readme file.
     */
    val heading: String,

    /**
     * The mermaid theme to be used for the generated graph.
     */
    val theme: Theme,

    /**
     * The orientation of the flowchart in the generated graph.
     */
    val orientation: Orientation,

    /**
     * The Pattern (Regex) to match nodes in the graph (project names) that should be highlighted and focused.
     * The graph will only show nodes that either point to the matched nodes or originate from them.
     *
     * The Value needs to be a string in regex format.
     */
    val focusedModulesRegex: String?,

    /**
     * Whether to add information as text on links in the graph.
     */
    val linkText: LinkText,

    /**
     * Whether to use custom styling for module nodes based on the plugin type.
     */
    val setStyleByModuleType: Boolean,

    /**
     * A Regex to match configurations that should be ignored.
     */
    val excludedConfigurationsRegex: String?,

    /**
     * A Regex to match modules that should be excluded from the graph.
     */
    val excludedModulesRegex: String?,

    /**
     * A Regex to match modules that should be used as root modules.
     * If this value is supplied,
     * the generated graph will only include dependencies (direct and transitive) of root modules.
     */
    val rootModulesRegex: String?,

    /**
     * Whether to show the full path of the module in the graph.
     * Use this if you have modules with the same name in different folders.
     * Note: when using this option, the graph generated won't use the subgraph feature mermaid provides.
     */
    val showFullPath: Boolean,
) : Serializable {
    companion object {
        /** Handles default values */
        fun create(
            readmePath: String?,
            heading: String?,
            theme: Theme? = null,
            orientation: Orientation? = null,
            focusedModulesRegex: String? = null,
            linkText: LinkText? = null,
            setStyleByModuleType: Boolean? = null,
            excludedConfigurationsRegex: String? = null,
            excludedModulesRegex: String? = null,
            rootModulesRegex: String? = null,
            showFullPath: Boolean? = null,
        ) = SingleGraphConfig(
            readmePath = readmePath ?: error("readmePath is a required parameter!"),
            heading = heading ?: error("heading is a required parameter!"),
            theme = theme ?: Theme.NEUTRAL,
            orientation = orientation ?: Orientation.LEFT_TO_RIGHT,
            linkText = linkText ?: LinkText.NONE,
            setStyleByModuleType = setStyleByModuleType ?: false,
            focusedModulesRegex = focusedModulesRegex,
            excludedConfigurationsRegex = excludedConfigurationsRegex,
            excludedModulesRegex = excludedModulesRegex,
            rootModulesRegex = rootModulesRegex,
            showFullPath = showFullPath ?: false,
        )
    }
}
