package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.Theme
import org.gradle.api.Plugin
import org.gradle.api.Project

private const val EXTENSION_NAME = "moduleGraphConfig"
private const val TASK_NAME = "createModuleGraph"

open class ModuleGraphPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(
            EXTENSION_NAME,
            ModuleGraphExtension::class.java,
            project,
        )

        project.tasks.register(
            TASK_NAME,
            CreateModuleGraphTask::class.java,
        ) { task ->
            task.heading.set(extension.heading)
            task.readmePath.set(extension.readmePath)
            task.theme.set(extension.theme)
            task.focusedNodesPattern.set(extension.focusedNodesPattern)
            task.orientation.set(extension.orientation)
            task.linkText.set(extension.linkText)
            task.showFullPath.set(extension.showFullPath)
            task.excludedConfigurationsRegex.set(extension.excludedConfigurationsRegex)
            task.excludedModulesRegex.set(extension.excludedModulesRegex)
            task.setStyleByModuleType.set(extension.setStyleByModuleType)
            task.outputFile.set(project.layout.projectDirectory.file(extension.readmePath))
            val projectGraphModel = project.parseProjectStructure(
                excludedConfigurations = extension.excludedConfigurationsRegex.orNull,
                excludedModules = extension.excludedModulesRegex.orNull,
                theme = extension.theme.getOrElse(Theme.NEUTRAL),
            )
            task.graphModel.set(projectGraphModel)
        }
    }
}
