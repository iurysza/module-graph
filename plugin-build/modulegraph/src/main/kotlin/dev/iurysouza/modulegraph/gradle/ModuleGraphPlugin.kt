package dev.iurysouza.modulegraph.gradle

import dev.iurysouza.modulegraph.externalDependencyCoordinates
import dev.iurysouza.modulegraph.snapshotAppliedPluginIds
import dev.iurysouza.modulegraph.gradle.graphparser.model.GradleProjectConfiguration
import dev.iurysouza.modulegraph.gradle.graphparser.model.ProjectInfo
import dev.iurysouza.modulegraph.model.GraphConfig
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

private const val EXTENSION_NAME = "moduleGraphConfig"
private const val TASK_NAME = "createModuleGraph"

/**
 * Applies the module graph to a single project: creates the `moduleGraphConfig` extension and the
 * `createModuleGraph` task, and contributes the project's own [ProjectInfo] snapshot.
 */
open class ModuleGraphPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.gradle.sharedServices.registerIfAbsent(
            ModuleGraphRegistry.NAME,
            ModuleGraphRegistry::class.java,
        ) {}
        val contribution = project.tasks.register(
            ModuleGraphContributeTask.NAME,
            ModuleGraphContributeTask::class.java,
        ) { task ->
            task.projectInfo.set(project.provider { project.collectProjectInfo() })
        }

        val extension = project.extensions.create(
            EXTENSION_NAME,
            ModuleGraphExtension::class.java,
            project,
        )

        project.tasks.register(
            TASK_NAME,
            CreateModuleGraphTask::class.java,
        ) { task ->

            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                task.doNotTrackState("https://github.com/iurysza/module-graph/issues/51")
            }

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
            task.projectDirectory.set(project.layout.projectDirectory)
            task.strictMode.set(extension.strictMode)
            task.nestingEnabled.set(extension.nestingEnabled)
            task.includeIsolatedModules.set(extension.includeIsolatedModules)
            task.dependsOn(contribution)

            val primaryGraphConfig = getPrimaryGraphConfig(task)
            val additionalGraphConfigs = task.graphConfigs.getOrElse(emptyList())
            val allGraphConfigs = listOfNotNull(primaryGraphConfig) + additionalGraphConfigs
            if (allGraphConfigs.isEmpty()) {
                error(
                    """
                    No valid graph configs were found!
                    Make sure to set up either the primary graph, or add additional graphs.
                    """.trimIndent(),
                )
            }
            task.graphConfigsResolved.set(allGraphConfigs)
        }
    }

    /** @return the primary graph config, or null if the primary config is not provided */
    private fun getPrimaryGraphConfig(task: CreateModuleGraphTask): GraphConfig? {
        val readmePath = task.readmePath.orNull
            ?: task.project.rootDir.resolve("README.md").absolutePath

        val heading = task.heading.orNull ?: "# Module Graph"

        val theme = task.theme.orNull
        val orientation = task.orientation.orNull
        val focusedModulesRegex = task.focusedModulesRegex.orNull
        val linkText = task.linkText.orNull
        val setStyleByModuleType = task.setStyleByModuleType.orNull
        val excludedConfigurationsRegex = task.excludedConfigurationsRegex.orNull
        val excludedModulesRegex = task.excludedModulesRegex.orNull
        val rootModulesRegex = task.rootModulesRegex.orNull
        val showFullPath = task.showFullPath.orNull
        val includeIsolatedModules = task.includeIsolatedModules.orNull
        val strictMode = task.strictMode.orNull
        val nestingEnabled = task.nestingEnabled.orNull

        val params: List<Any?> = listOf(
            readmePath,
            heading,
            theme,
            orientation,
            focusedModulesRegex,
            linkText,
            setStyleByModuleType,
            excludedConfigurationsRegex,
            excludedModulesRegex,
            rootModulesRegex,
            showFullPath,
            includeIsolatedModules,
            strictMode,
            nestingEnabled,
        )

        /**
         * This is true if any primary config parameter is provided,
         * which would indicate that the consumer is trying to setup a primary config
         */
        val hasPrimaryConfig = params.any { it != null }
        // If the user is not trying to setup the primary config,
        // then there is no primary config - simple as
        if (!hasPrimaryConfig) return null

        return GraphConfig.Builder(
            readmePath = readmePath,
            heading = heading,
        ).apply {
            this.theme = theme
            this.orientation = orientation
            this.linkText = linkText
            this.setStyleByModuleType = setStyleByModuleType
            this.showFullPath = showFullPath
            this.includeIsolatedModules = includeIsolatedModules
            this.excludedConfigurationsRegex = excludedConfigurationsRegex
            this.excludedModulesRegex = excludedModulesRegex
            this.rootModulesRegex = rootModulesRegex
            this.focusedModulesRegex = focusedModulesRegex
            this.strictMode = strictMode
            this.nestingEnabled = nestingEnabled
        }.build()
    }
}

/** @return a [ProjectInfo] snapshot of the given project's own configurations and plugins. */
internal fun Project.collectProjectInfo(): ProjectInfo {
    val collectedConfigurations = configurations.map { configuration ->
        val projectPaths = configuration.dependencies
            .withType(ProjectDependency::class.java)
            .map { dependency -> dependency.path }
        GradleProjectConfiguration(configuration.name, projectPaths)
    }
    return ProjectInfo(
        path = path,
        pluginIds = snapshotAppliedPluginIds(),
        externalDependencies = externalDependencyCoordinates(),
        configurations = collectedConfigurations,
    )
}
