package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.gradle.graphparser.model.ProjectInfo
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Registers a single project's [ProjectInfo] into the [ModuleGraphRegistry] at execution time.
 *
 * The snapshot is captured as a serializable input during configuration and registered when the
 * task runs.
 */
internal abstract class ModuleGraphContributeTask : DefaultTask() {
    @get:Input
    abstract val projectInfo: Property<ProjectInfo>

    @Suppress("UnstableApiUsage")
    @get:ServiceReference(ModuleGraphRegistry.NAME)
    abstract val registry: Property<ModuleGraphRegistry>

    @TaskAction
    fun contribute() {
        registry.get().register(projectInfo.get())
    }

    companion object {
        const val NAME: String = "moduleGraphContribute"
    }
}
