package dev.iurysouza.modulegraph

import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "moduleGraphConfig"
const val TASK_NAME = "createModuleGraph"

abstract class ModuleGraphPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(EXTENSION_NAME, ModuleGraphExtension::class.java, project)

        project.tasks.register(TASK_NAME, CreateModuleGraphTask::class.java) {
            it.heading.set(extension.heading)
            it.readmeFile.set(extension.readmeFile)
        }
    }
}
