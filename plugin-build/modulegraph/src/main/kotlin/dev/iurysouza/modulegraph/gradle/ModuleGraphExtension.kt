package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.LinkText
import dev.iurysouza.modulegraph.Orientation
import dev.iurysouza.modulegraph.Theme
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Property

/**
 * The ModuleGraphExtension is used for configuring the module graph generation in a project.
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
     * Whether to show the full path of the module in the graph.
     * Use this if you have modules with the same name in different folders.
     * Note: when using this option, the graph generated won't use the subgraph feature mermaid provides.
     */
    val showFullPath: Property<Boolean> = objects.property(Boolean::class.java)
}
