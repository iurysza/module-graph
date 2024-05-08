package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.gradle.graphparser.ProjectParser
import dev.iurysouza.modulegraph.gradle.graphparser.projectquerier.GradleProjectQuerier
import dev.iurysouza.modulegraph.model.ProjectGraphResult
import dev.iurysouza.modulegraph.model.SingleGraphConfig
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
            task.focusedModulesRegex.set(extension.focusedModulesRegex)
            task.orientation.set(extension.orientation)
            task.linkText.set(extension.linkText)
            task.showFullPath.set(extension.showFullPath)
            task.excludedConfigurationsRegex.set(extension.excludedConfigurationsRegex)
            task.excludedModulesRegex.set(extension.excludedModulesRegex)
            task.setStyleByModuleType.set(extension.setStyleByModuleType)
            task.rootModulesRegex.set(extension.rootModulesRegex)
            task.graphConfigs.set(extension.graphConfigs)

            val primaryGraphConfig = SingleGraphConfig.create(
                readmePath = task.readmePath.orNull,
                heading = task.heading.orNull,
                theme = task.theme.orNull,
                orientation = task.orientation.orNull,
                focusedModulesRegex = task.focusedModulesRegex.orNull,
                linkText = task.linkText.orNull,
                setStyleByModuleType = task.setStyleByModuleType.orNull,
                excludedConfigurationsRegex = task.excludedConfigurationsRegex.orNull,
                excludedModulesRegex = task.excludedConfigurationsRegex.orNull,
                rootModulesRegex = task.rootModulesRegex.orNull,
                showFullPath = task.showFullPath.orNull,
            )
            val additionalGraphConfigs = task.graphConfigs.getOrElse(emptyList())
            val allGraphConfigs = listOf(primaryGraphConfig) + additionalGraphConfigs

            val allProjects = project.allprojects
            val allProjectPaths = allProjects.map { it.path }
            val projectQuerier = GradleProjectQuerier(allProjects)

            val results = allGraphConfigs.map { config ->
                val projectGraph = ProjectParser.parseProjectGraph(
                    allProjectPaths = allProjectPaths,
                    config = config,
                    projectQuerier = projectQuerier,
                )
                ProjectGraphResult(projectGraph, config)
            }

            task.graphModels.set(results)
        }
    }
}
