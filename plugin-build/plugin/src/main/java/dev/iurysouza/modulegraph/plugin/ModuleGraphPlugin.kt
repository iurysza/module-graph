package dev.iurysouza.modulegraph.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "moduleGraphConfig"
const val TASK_NAME = "createModuleGraph"

abstract class TemplatePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Add the 'template' extension object
        val extension = project.extensions.create(EXTENSION_NAME, ModuleGraphExtension::class.java, project)

        // Add a task that uses configuration from the extension object
        project.tasks.register(TASK_NAME, CreateModuleGraphTask::class.java) {
            it.heading.set(extension.heading)
            it.readmeFile.set(extension.readmeFile)
        }
    }
}
