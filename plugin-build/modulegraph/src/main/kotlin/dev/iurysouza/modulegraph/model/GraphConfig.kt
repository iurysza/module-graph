package dev.iurysouza.modulegraph.model

import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme
import java.io.Serializable as JavaSerializable
import kotlinx.serialization.Serializable

/**
 * The config for a single graph to be made.
 */
@Serializable
data class GraphConfig(
    /* Output parameters */

    /**
     * The path of the readme file where the graph will be appended.
     */
    val readmePath: String,

    /**
     * The heading under which the graph will be appended in the readme file.
     */
    val heading: String,

    /* Styling parameters */

    /** @see Theme */
    val theme: Theme,

    /** @see Orientation */
    val orientation: Orientation,

    /** @see LinkText */
    val linkText: LinkText,

    /**
     * Whether to use custom styling for module nodes based on the plugin type.
     */
    val setStyleByModuleType: Boolean,

    /**
     * Whether to show the full path of the module in the graph.
     * Use this if you have modules with the same name in different folders.
     * Note: when using this option, the graph generated won't use the subgraph feature mermaid provides.
     */
    val showFullPath: Boolean,

    /* Content regex pattern parameters */

    /**
     * The Pattern (Regex) to match nodes in the graph (project names) that should be highlighted and focused.
     * The graph will only show nodes that either point to the matched nodes or originate from them.
     *
     * The Value needs to be a string in regex format.
     */
    val focusedModulesRegex: String?,

    /**
     * A Regex to match configurations that should be ignored.
     */
    val excludedConfigurationsRegex: String?,

    /**
     * A Regex to match modules that should be excluded from the graph.
     */
    val excludedModulesRegex: String?,

    /**
     * Whether to fail the task if no modules are found matching the specified criteria
     * (e.g., focusedModulesRegex, rootModulesRegex).
     * If set to `false`, the task will silently no-op if no modules are found.
     */
    val strictMode: Boolean,

    /**
     * Whether to enable nested subgraphs in the generated graph.
     * When enabled, modules will be grouped into subgraphs based on their path structure.
     * For example, modules under ":libs:feature1" and ":libs:feature2" will be nested under a ":libs" subgraph.
     * Note: This option is ignored if [showFullPath] is true.
     */
    val nestingEnabled: Boolean,

    /**
     * A Regex to match modules that should be used as root modules.
     * If this value is supplied,
     * the generated graph will only include dependencies (direct and transitive) of root modules.
     */
    val rootModulesRegex: String?,
) : JavaSerializable {

    class Builder(
        /** @see [GraphConfig.readmePath] */
        val readmePath: String,

        /** @see [GraphConfig.heading] */
        val heading: String,
    ) {
        /** @see [GraphConfig.theme] */
        var theme: Theme? = null

        /** @see [GraphConfig.orientation] */
        var orientation: Orientation? = null

        /** @see [GraphConfig.linkText] */
        var linkText: LinkText? = null

        /** @see [GraphConfig.setStyleByModuleType] */
        var setStyleByModuleType: Boolean? = null

        /** @see [GraphConfig.showFullPath] */
        var showFullPath: Boolean? = null

        /** @see [GraphConfig.excludedConfigurationsRegex] */
        var excludedConfigurationsRegex: String? = null

        /** @see [GraphConfig.excludedModulesRegex] */
        var excludedModulesRegex: String? = null

        /** @see [GraphConfig.rootModulesRegex] */
        var rootModulesRegex: String? = null

        /** @see [GraphConfig.focusedModulesRegex] */
        var focusedModulesRegex: String? = null

        /**
         * See [GraphConfig.strictMode]
         */
        var strictMode: Boolean? = null

        /**
         * See [GraphConfig.nestingEnabled]
         */
        var nestingEnabled: Boolean? = null

        internal fun build(): GraphConfig {
            return GraphConfig(
                readmePath = readmePath,
                heading = heading,
                theme = theme ?: Theme.NEUTRAL,
                orientation = orientation ?: Orientation.LEFT_TO_RIGHT,
                linkText = linkText ?: LinkText.NONE,
                setStyleByModuleType = setStyleByModuleType ?: false,
                focusedModulesRegex = focusedModulesRegex,
                excludedConfigurationsRegex = excludedConfigurationsRegex,
                excludedModulesRegex = excludedModulesRegex,
                rootModulesRegex = rootModulesRegex,
                showFullPath = showFullPath ?: false,
                strictMode = strictMode ?: false,
                nestingEnabled = nestingEnabled ?: false,
            )
        }
    }
}
