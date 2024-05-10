package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.gradle.graphparser.ProjectParser
import dev.iurysouza.modulegraph.gradle.graphparser.projectquerier.GradleProjectQuerier
import dev.iurysouza.modulegraph.model.GraphConfig
import dev.iurysouza.modulegraph.model.GraphParseResult
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

            val primaryGraphConfig = getPrimaryConfig(task)
            val additionalGraphConfigs = task.graphConfigs.getOrElse(emptyList())
            val allGraphConfigs = listOfNotNull(primaryGraphConfig) + additionalGraphConfigs
            if (allGraphConfigs.isEmpty()) {
                error("No valid graph configs were found! Make sure to set up either the primary graph, or add additional graphs")
            }

            val allProjects = project.allprojects
            val allProjectPaths = allProjects.map { it.path }
            val projectQuerier = GradleProjectQuerier(allProjects)

            val results = allGraphConfigs.map { config ->
                val projectGraph = ProjectParser.parseProjectGraph(
                    allProjectPaths = allProjectPaths,
                    config = config,
                    projectQuerier = projectQuerier,
                )
                GraphParseResult(projectGraph, config)
            }

            task.graphModels.set(results)
        }
    }

    /** @return the primary graph config, or null if the primary config is not setup correctly */
    private fun getPrimaryConfig(task: CreateModuleGraphTask): GraphConfig? {
        val readmePath = task.readmePath.orNull ?: return null
        val heading = task.heading.orNull ?: return null
        return GraphConfig.Builder(
            readmePath = readmePath,
            heading = heading,
        ).apply {
            theme = task.theme.orNull
            orientation = task.orientation.orNull
            focusedModulesRegex = task.focusedModulesRegex.orNull
            linkText = task.linkText.orNull
            setStyleByModuleType = task.setStyleByModuleType.orNull
            excludedConfigurationsRegex = task.excludedConfigurationsRegex.orNull
            excludedModulesRegex = task.excludedConfigurationsRegex.orNull
            rootModulesRegex = task.rootModulesRegex.orNull
            showFullPath = task.showFullPath.orNull
        }.build()
    }
}
